package com.bytezone.plugintest;

import com.bytezone.dm3270.plugins.DefaultPlugin;
import com.bytezone.dm3270.plugins.PluginData;

public class ShowDataset extends DefaultPlugin
{
  DatasetStage datasetStage;

  @Override
  public void activate ()
  {
    if (datasetStage == null)
      datasetStage = new DatasetStage ();
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
    return true;
  }

  @Override
  public void processRequest (PluginData data)
  {
    datasetStage.showDataset (data, getModifiableFields (data));
  }
}