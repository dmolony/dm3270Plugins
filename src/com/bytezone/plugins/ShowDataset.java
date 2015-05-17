package com.bytezone.plugins;

import java.util.Map;
import java.util.TreeMap;

import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.plugins.DefaultPlugin;
import com.bytezone.dm3270.plugins.PluginData;

public class ShowDataset extends DefaultPlugin
{
  private final Map<String, Document> documents = new TreeMap<> ();
  private final DatasetStage datasetStage = new DatasetStage ();
  private Document currentDocument;
  private boolean doesAuto;
  private boolean doesRequest;

  private int loopCount;
  private final int maxLoops = 20;

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
    currentDocument = null;
    loopCount = 0;

    DocumentPage page = DocumentPage.createPage (data, getModifiableFields (data));
    if (page == null)
    {
      System.out.println ("Not a document page");
      return;
    }

    if (page.firstLine != 1)
    {
      data.key = AIDCommand.AID_PF7;
      setMax (data);
      return;
    }

    if (page.leftColumn != 1)
    {
      data.key = AIDCommand.AID_PF10;
      setMax (data);
      return;
    }

    setCurrentDocument (page);

    if (currentDocument.isComplete ())
      datasetStage.showDataset (currentDocument);
    else
      doesAuto = true;
  }

  @Override
  public void processAuto (PluginData data)
  {
    if (++loopCount > maxLoops)
    {
      System.out.println ("loop count exceeded");
      doesAuto = false;
      return;
    }

    DocumentPage page = DocumentPage.createPage (data, getModifiableFields (data));
    if (page == null)
    {
      System.out.println ("Not a document page");
      doesAuto = false;
      return;
    }

    if (currentDocument == null)
    {
      if (page.firstLine != 1)
      {
        System.out.println ("Not at document first document line");
        doesAuto = false;
        return;
      }

      if (page.leftColumn != 1)       // this could loop
      {
        data.key = AIDCommand.AID_PF10;
        setMax (data);
        return;
      }

      setCurrentDocument (page);
    }
    else
      currentDocument.addDocumentPage (page);

    if (currentDocument.isComplete ())
      datasetStage.showDataset (currentDocument);
    else
    {
      // scroll to next page
      if (page.leftColumn == 1)
      {
        if (page.hasEnd)
        {
          data.key = AIDCommand.AID_PF11;
          return;
        }
        else
        {
          data.key = AIDCommand.AID_PF8;
          return;
        }
      }
      else
      {
        if (page.hasBeginning)
        {
          data.key = AIDCommand.AID_PF10;
          setMax (data);
          doesAuto = false;
          return;
        }
        else
        {
          data.key = AIDCommand.AID_PF7;
          return;
        }
      }
    }
  }

  private void setMax (PluginData data)
  {

  }

  private void setCurrentDocument (DocumentPage page)
  {
    String name = page.fullName;
    if (documents.containsKey (name))
    {
      currentDocument = documents.get (name);
      currentDocument.addDocumentPage (page);
    }
    else
      currentDocument = new Document (page);
  }
}