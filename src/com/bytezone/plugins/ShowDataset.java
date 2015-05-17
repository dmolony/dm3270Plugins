package com.bytezone.plugins;

import java.util.Map;
import java.util.TreeMap;

import com.bytezone.dm3270.plugins.DefaultPlugin;
import com.bytezone.dm3270.plugins.PluginData;

public class ShowDataset extends DefaultPlugin
{
  Map<String, Document> documents = new TreeMap<> ();
  DatasetStage datasetStage = new DatasetStage ();
  Document currentDocument;
  private boolean doesAuto;
  private boolean doesRequest;

  @Override
  public void activate ()
  {
  }

  @Override
  public void deactivate ()
  {
    if (datasetStage != null)
      datasetStage.hide ();
  }

  @Override
  public boolean doesRequest ()
  {
    return doesRequest;
  }

  @Override
  public boolean doesAuto ()
  {
    return doesAuto;
  }

  @Override
  public void processRequest (PluginData data)
  {
    DocumentPage page = DocumentPage.createPage (data, getModifiableFields (data));
    String name = page.fullName;
    if (documents.containsKey (name))
      currentDocument = documents.get (name);
    else
      currentDocument = new Document (page);
    currentDocument.addDocumentPage (page);

    if (currentDocument.isComplete ())
      datasetStage.showDataset (currentDocument);
  }

  @Override
  public void processAuto (PluginData data)
  {
    DocumentPage page = DocumentPage.createPage (data, getModifiableFields (data));
    currentDocument.addDocumentPage (page);

    if (currentDocument.isComplete ())
      datasetStage.showDataset (currentDocument);
  }
}