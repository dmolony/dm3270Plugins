package com.bytezone.plugins;

import java.util.ArrayList;
import java.util.List;

public class Document
{
  final String datasetName;
  final String memberName;
  int maxColumns;
  int totalLines;

  List<DocumentPage> pages = new ArrayList<> ();

  public Document (DocumentPage page)
  {
    datasetName = page.datasetName;
    memberName = page.memberName;

    addDocumentPage (page);
  }

  public void addDocumentPage (DocumentPage page)
  {
    assert datasetName.equals (page.datasetName);
    assert memberName.equals (page.memberName);

    boolean found = false;
    for (DocumentPage dp : pages)
    {
      if (dp.matches (page))
      {
        found = true;
        break;
      }
    }

    if (!found)
    {
      pages.add (page);
      System.out.println ("adding");
    }
    else
      System.out.println ("replacing");

    System.out.println (page);
  }

  public boolean isComplete ()
  {
    return false;
  }
}