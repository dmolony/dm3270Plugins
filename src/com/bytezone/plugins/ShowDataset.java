package com.bytezone.plugins;

import java.util.Map;
import java.util.TreeMap;

import com.bytezone.dm3270.commands.AIDCommand;
import com.bytezone.dm3270.plugins.DefaultPlugin;
import com.bytezone.dm3270.plugins.PluginData;
import com.bytezone.dm3270.plugins.PluginField;

public class ShowDataset extends DefaultPlugin
{
  private final Map<String, Document> documents = new TreeMap<> ();
  private final DatasetStage datasetStage = new DatasetStage ();
  private Document currentDocument;
  private boolean doesAuto;
  private boolean doesRequest;

  private int loopCount;
  private final int maxLoops = 20;
  private DocumentPage previousPage;

  private boolean pendingBottomRight;
  private boolean[][] visitedPages;
  private int unvisitedPages = -1;

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
    previousPage = null;
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

    if (page.hasEnd)
      data.key = AIDCommand.AID_PF11;
    else
      data.key = AIDCommand.AID_PF8;
    doesAuto = true;
  }

  @Override
  public void processAuto (PluginData data)
  {
    System.out.printf ("Loopcount %d%n", loopCount);
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

    if (pendingBottomRight)
    {
      pendingBottomRight = false;
      prepareVisitorGrid (page.lastLine, page.rightColumn);
    }

    if (page.matches (previousPage))
    {
      System.out.println ("We're done");
      doesAuto = false;
      return;
    }

    previousPage = page;

    if (currentDocument == null)
    {
      if (page.firstLine != 1)
      {
        System.out.println ("Not at document first document line");
        doesAuto = false;
        return;
      }

      if (page.leftColumn != 1)        // this could loop
      {
        data.key = AIDCommand.AID_PF10;
        setMax (data);
        return;
      }

      setCurrentDocument (page);
    }
    else
      currentDocument.addDocumentPage (page);

    System.out.println (currentDocument);

    System.out.println ("Where to now?");
    // scroll to next page
    if (page.leftColumn == 1)
    {
      if (page.hasEnd)
      {
        data.key = AIDCommand.AID_PF11;       // go max right
        setMax (data);
        System.out.println ("go right max");
        pendingBottomRight = true;
        return;
      }
      else
      {
        data.key = AIDCommand.AID_PF8;        // go down
        System.out.println ("go down");
        return;
      }
    }
    else
    {
      if (page.hasBeginning)
      {
        data.key = AIDCommand.AID_PF10;       // go left (assumes only one circuit)
        setMax (data);
        doesAuto = false;
        System.out.println ("go left max");
        datasetStage.setDocument (currentDocument);
        datasetStage.show ();
        return;
      }
      else
      {
        data.key = AIDCommand.AID_PF7;        // go up
        System.out.println ("go up");
        return;
      }
    }
  }

  private void setMax (PluginData data)
  {
    PluginField commandField = data.getField ("Command ===>");
    if (commandField != null)
    {
      PluginField inputField = data.getField (commandField.sequence + 1);
      inputField.change ("m");
    }
  }

  private void prepareVisitorGrid (int rows, int columns)
  {
    // divide these by the page size
    int pageRows = (rows - 1) / 20 + 1;
    int pageColumns = (columns - 1) / 72 + 1;

    currentDocument.maxColumns = columns;
    currentDocument.totalLines = rows;
    visitedPages = new boolean[pageRows][pageColumns];
    for (int i = 0; i < pageRows; i++)
      visitedPages[i][0] = true;
    visitedPages[pageRows - 1][pageColumns - 1] = true;
    int visitedPages = pageRows + (pageColumns > 1 ? 1 : 0);
    unvisitedPages = pageRows * pageColumns - visitedPages;

    System.out.printf ("Grid %d rows x %d columns%n", pageRows, pageColumns);
    System.out.printf ("Visited: %d, unvisited: %d%n", visitedPages, unvisitedPages);
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