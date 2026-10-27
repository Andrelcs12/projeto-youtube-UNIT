package com.media.transcoder.posts.service;

import com.media.transcoder.jobs.dto.PostResponse;
import com.media.transcoder.jobs.service.JobService;
import java.util.List;

public class PostService {
    private final JobService jobs;
    public PostService(JobService jobs){this.jobs=jobs;}
    public List<PostResponse> list(){return jobs.list().stream().map(job->new PostResponse(job.id(),job.originalName(),"Vídeo convertido para "+String.join(", ",job.requestedProfiles()),job.status(),job.createdAt(),job.id().toString(),job.outputs().isEmpty()?null:job.outputs().get(0).downloadUrl())).toList();}
}
