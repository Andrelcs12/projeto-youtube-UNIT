package com.media.transcoder.jobs.service;

import com.media.transcoder.integration.ffmpeg.FfmpegService;
import com.media.transcoder.jobs.model.Job;
import com.media.transcoder.jobs.model.JobStatus;
import com.media.transcoder.jobs.repository.JobRepository;
import com.media.transcoder.shared.ApiException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class JobQueue implements AutoCloseable {
    private final ThreadPoolExecutor executor; private final FfmpegService ffmpeg; private final JobRepository jobs;
    public JobQueue(int capacity,int workers,FfmpegService ffmpeg,JobRepository jobs){this.ffmpeg=ffmpeg;this.jobs=jobs;this.executor=new ThreadPoolExecutor(workers,workers,0,TimeUnit.MILLISECONDS,new ArrayBlockingQueue<>(capacity),r->{Thread t=new Thread(r,"video-worker");t.setDaemon(true);return t;},new ThreadPoolExecutor.AbortPolicy());}
    public void submit(Job job){try{executor.execute(()->process(job));}catch(java.util.concurrent.RejectedExecutionException e){throw new ApiException(429,"Fila cheia.");}}
    private void process(Job job){job.status(JobStatus.PROCESSING);try{ffmpeg.convert(job);job.status(JobStatus.COMPLETED);}catch(Exception e){job.fail(safeMessage(e));}finally{jobs.save(job);}}
    private String safeMessage(Exception e){String message=e.getMessage();return message==null?"Falha durante a conversão.":message.substring(0,Math.min(message.length(),300));}
    public int queued(){return executor.getQueue().size();} public int capacity(){return executor.getQueue().size()+executor.getQueue().remainingCapacity();}
    @Override public void close(){executor.shutdown();try{if(!executor.awaitTermination(10,TimeUnit.SECONDS))executor.shutdownNow();}catch(InterruptedException e){executor.shutdownNow();Thread.currentThread().interrupt();}}
}
