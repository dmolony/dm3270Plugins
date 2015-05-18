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
  List<Line> lines = new ArrayList<> ();

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

  public List<Line> getLines ()
  {
    if (lines.size () == 0)
      stitch ();
    return lines;
  }

  private void stitch ()
  {
    int lineNo = 0;
    for (DocumentPage page : pages)
    {
      if (page.leftColumn == 1)
      {
        int count = 0;
        for (String text : page.lines)
        {
          String number = page.numbers.get (count++);
          Line line = new Line ();
          line.text = String.format ("%s %s", number, text);
          line.leftColumn = page.leftColumn;
          line.rightColumn = page.rightColumn;
          lines.add (line);
        }
      }
      else
      {
        for (String text : page.lines)
        {
          Line line = lines.get (lineNo++);
          line.text += text;      // wrong but closer
          line.rightColumn = page.rightColumn;
        }
      }
    }
  }

  class Line
  {
    String text;
    int leftColumn;
    int rightColumn;

    @Override
    public String toString ()
    {
      return text;
    }
  }
}