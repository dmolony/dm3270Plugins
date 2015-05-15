package com.bytezone.plugins;

import com.bytezone.dm3270.plugins.DefaultPlugin;
import com.bytezone.dm3270.plugins.PluginData;

public class ShowDataset extends DefaultPlugin
{
  DatasetStage datasetStage = new DatasetStage ();

  @Override
  public void activate ()
  {
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
    return datasetStage.doesRequest ();
  }

  @Override
  public boolean doesAuto ()
  {
    return datasetStage.doesAuto ();
  }

  @Override
  public void processRequest (PluginData data)
  {
    datasetStage.showDataset (data, getModifiableFields (data));
  }

  @Override
  public void processAuto (PluginData data)
  {
    datasetStage.showDataset (data, getModifiableFields (data));
  }
}