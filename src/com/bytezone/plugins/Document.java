package com.bytezone.plugins;

import java.util.ArrayList;
import java.util.Collections;
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
    int index = 0;
    for (DocumentPage dp : pages)
    {
      if (dp.matches (page))
      {
        found = true;
        break;
      }
      index++;
    }

    if (!found)
    {
      pages.add (page);
      System.out.println ("adding");
    }
    else
    {
      pages.add (index, page);
      System.out.println ("replacing");
    }

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
    Collections.sort (pages);

    for (DocumentPage page : pages)
    {
      if (page.rightColumn > maxColumns)
        maxColumns = page.rightColumn;

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
        int col = page.leftColumn + 6;
        String format = "%-" + col + "." + col + "s%s";
        for (String text : page.lines)
        {
          Line line = lines.get (lineNo++);
          line.text = String.format (format, line.text, text);
          line.rightColumn = page.rightColumn;
        }
      }
    }
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Dataset name ... %s%n", datasetName));
    text.append (String.format ("Member name .... %s%n", memberName));
    text.append (String.format ("Lines .......... %d%n", totalLines));
    text.append (String.format ("Columns ........ %d", maxColumns));

    return text.toString ();
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