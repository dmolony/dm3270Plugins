package com.bytezone.plugins;

import java.util.ArrayList;
import java.util.List;

public class Document
{
  List<DocumentPage> pages = new ArrayList<> ();

  public Document (DocumentPage page)
  {
    addDocumentPage (page);
  }

  public void addDocumentPage (DocumentPage page)
  {
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