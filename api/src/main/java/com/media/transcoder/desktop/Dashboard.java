package com.media.transcoder.desktop;

import com.fasterxml.jackson.databind.JsonNode;
import com.media.transcoder.shared.Json;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Dashboard {
    private final String baseUrl;
    private final DefaultTableModel tableModel=new DefaultTableModel(new String[]{"ID","Arquivo","Status","Perfis"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
    private final JTable table=new JTable(tableModel); private final JLabel message=new JLabel(" ");
    public Dashboard(String baseUrl){this.baseUrl=baseUrl;}
    public void show(){if(!SwingUtilities.isEventDispatchThread()){SwingUtilities.invokeLater(this::show);return;}JFrame frame=new JFrame("Transcoder Video — Monitor");frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);frame.setSize(860,480);frame.setLocationRelativeTo(null);frame.add(new JLabel("Jobs de processamento local",SwingConstants.CENTER),BorderLayout.NORTH);frame.add(new JScrollPane(table),BorderLayout.CENTER);JPanel footer=new JPanel(new BorderLayout());footer.add(message,BorderLayout.CENTER);JButton refresh=new JButton("Atualizar");refresh.addActionListener(e->load());footer.add(refresh,BorderLayout.EAST);frame.add(footer,BorderLayout.SOUTH);frame.setVisible(true);load();new Timer(5000,e->load()).start();}
    private void load(){HttpClient.newHttpClient().sendAsync(HttpRequest.newBuilder(URI.create(baseUrl+"/jobs")).GET().build(),HttpResponse.BodyHandlers.ofString()).thenAccept(response->{try{if(response.statusCode()!=200)throw new IllegalStateException("API HTTP "+response.statusCode());JsonNode root=Json.MAPPER.readTree(response.body());SwingUtilities.invokeLater(()->{tableModel.setRowCount(0);for(JsonNode job:root){StringBuilder profiles=new StringBuilder();for(JsonNode output:job.path("outputs")){if(profiles.length()>0)profiles.append(", ");profiles.append(output.path("profile").asText());}tableModel.addRow(new Object[]{job.path("id").asText(),job.path("originalName").asText(),job.path("status").asText(),profiles.toString()});}message.setText("Atualizado agora · "+root.size()+" job(s)");});}catch(Exception e){SwingUtilities.invokeLater(()->message.setText("Falha ao consultar API: "+e.getMessage()));}}).exceptionally(error->{SwingUtilities.invokeLater(()->message.setText("API indisponível: "+error.getMessage()));return null;});}
}
