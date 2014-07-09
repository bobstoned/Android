package fester.Festaxian;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import com.example.R;

/**
 * Created with IntelliJ IDEA.
 * User: rlester
 * Date: 06/08/12
 * Time: 13:09
 * To change this template use File | Settings | File Templates.
 */
public class FestaxianSettings extends PreferenceActivity {
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);
  }
}