package com.bytezone.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bytezone.dm3270.plugins.PluginData;
import com.bytezone.dm3270.plugins.PluginField;

public class DocumentPage implements Comparable<DocumentPage>
{
  private static final String START_DATA =
      "***************************** Top of Dat" + "a ******************************";
  private static final String END_DATA =
      "**************************** Bottom of D" + "ata ****************************";
  private static String pattern = "([A-Z0-9]{1,8}(\\.[A-Z0-9]{1,8})*)" // dataset name
      + "(\\([A-Z0-9]{1,8}\\))?"                        // member name
      + "( - [0-9]{2}\\.[0-9]{2})?";                    // editing data
  private static final Pattern p = Pattern.compile (pattern);

  String datasetName;
  String memberName;
  String fullName;

  int firstLine = -1;
  int lastLine;

  boolean hasBeginning;
  boolean hasEnd;

  int leftColumn;
  int rightColumn;

  List<String> numbers = new ArrayList<> ();
  List<String> lines = new ArrayList<> ();

  public static DocumentPage createPage (PluginData data,
      List<PluginField> modifiableFields)
  {
    PluginField editField = findField ("EDIT", data);
    if (editField == null)
      return null;

    PluginField commandField = findField ("Command ===>", data);
    if (commandField == null || commandField.sequence < editField.sequence)
      return null;

    PluginField scrollField = findField ("Scroll ===>", data);
    if (scrollField == null || scrollField.sequence < commandField.sequence)
      return null;

    return new DocumentPage (data, modifiableFields);
  }

  private DocumentPage (PluginData data, List<PluginField> modifiableFields)
  {
    getDatasetName (data);
    getColumns (data);

    for (PluginField sf : modifiableFields)
      switch (sf.location.column)
      {
        case 1:
          if (sf.getLength () == 6 && sf.getFieldValue ().equals ("******"))
          {
            PluginField nextField = data.getField (sf.sequence + 1);
            if (nextField != null && nextField.isProtected
                && nextField.getLength () >= 72)
            {
              if (nextField.getFieldValue ().startsWith (START_DATA))
                hasBeginning = true;
              else if (nextField.getFieldValue ().startsWith (END_DATA))
                hasEnd = true;
            }
          }
          else
            numbers.add (sf.getFieldValue ());
          break;

        case 8:
          lines.add (sf.getFieldValue ());
          break;

        case 14:      // command field
        case 75:      // CSR/PAGE
          break;

        default:
          System.out.printf ("column %d: %s%n", sf.location.column, sf.getFieldValue ());
      }

    if (lines.size () > 0)
    {
      firstLine = Integer.parseInt (numbers.get (0));
      lastLine = Integer.parseInt (numbers.get (numbers.size () - 1));
    }
  }

  public boolean matches (DocumentPage otherPage)
  {
    if (otherPage == null)
      return false;

    return leftColumn == otherPage.leftColumn && rightColumn == otherPage.rightColumn
        && firstLine >= 0 && firstLine == otherPage.firstLine;
  }

  private void getDatasetName (PluginData data)
  {

    datasetName = data.trimField (12);
    Matcher m = p.matcher (datasetName);
    if (m.matches ())
    {
      if (false)
      {
        System.out.println (m.groupCount ());
        for (int i = 0; i <= m.groupCount (); i++)
          System.out.printf ("%2d %s%n", i, m.group (i));
      }

      datasetName = m.group (1);
      if (m.groupCount () >= 3)
      {
        String name = m.group (3);
        memberName = name.substring (1, name.length () - 1);
        fullName = String.format ("%s(%s)", datasetName, memberName);
      }
      else
      {
        memberName = "";
        fullName = datasetName;
      }
    }
  }

  private void getColumns (PluginData data)
  {
    PluginField columnsField = findField ("Columns", data);        // may not exist

    if (columnsField == null || columnsField.getLength () != 7
        || columnsField.isModifiable)
      return;

    String col1 = data.trimField (columnsField.sequence + 1);
    String col2 = data.trimField (columnsField.sequence + 2);

    try
    {
      leftColumn = Integer.parseInt (col1);
      rightColumn = Integer.parseInt (col2);
    }
    catch (NumberFormatException e)
    {
      System.out.println ("Bad column number format");
      leftColumn = 0;
      rightColumn = 0;
    }
  }

  private static PluginField findField (String text, PluginData data)
  {
    for (PluginField screenField : data.screenFields)
      if (text.equals (screenField.getFieldValue ().trim ()))
        return screenField;
    return null;
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder ();

    text.append (String.format ("Dataset name .... %s%n", datasetName));
    text.append (String.format ("Member name ..... %s%n", memberName));
    text.append (String.format ("Lines ........... %d%n", lines.size ()));
    text.append (String.format ("First line ...... %d%n", firstLine));
    text.append (String.format ("Last line ....... %d%n", lastLine));
    text.append (String.format ("Has first ....... %s%n", hasBeginning));
    text.append (String.format ("Has last ........ %s%n", hasEnd));
    text.append (String.format ("Left column ..... %d%n", leftColumn));
    text.append (String.format ("Right column .... %d%n", rightColumn));
    text.append ("\n");

    for (int i = 0; i < lines.size (); i++)
      text.append (String.format ("%s %s%n", numbers.get (i), lines.get (i)));

    return text.toString ();
  }

  @Override
  public int compareTo (DocumentPage o)
  {
    if (leftColumn == o.leftColumn)
      return firstLine - o.firstLine;
    return leftColumn - o.leftColumn;
  }
}