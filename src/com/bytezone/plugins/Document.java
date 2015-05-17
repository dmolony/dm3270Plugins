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
    for (DocumentPage dp : pages)
    {
      if (dp.matches (page))
      {

      }
    }
  }

  public boolean isComplete ()
  {
    return false;
  }
}