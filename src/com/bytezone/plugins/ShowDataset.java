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
    doesAuto = false;
    doesRequest = true;
  }

  @Override
  public void deactivate ()
  {
    if (datasetStage != null)
      datasetStage.hide ();

    doesAuto = false;
    doesRequest = false;
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
    if (page == null)
    {
      System.out.println ("Not a document page");
      return;
    }

    String name = page.fullName;
    if (documents.containsKey (name))
    {
      currentDocument = documents.get (name);
      currentDocument.addDocumentPage (page);
    }
    else
      currentDocument = new Document (page);

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