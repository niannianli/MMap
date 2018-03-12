package li.com.mmap;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class SingleItemDetailsActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.rv2_single_item_details);

        ImageView imageView3 = findViewById(R.id.imageView3);
        TextView nameTextView3 = findViewById(R.id.textViewName3);
        TextView geoTextView3 = findViewById(R.id.textViewGeo3);
        TextView addressTextView3 = findViewById(R.id.textViewAddress3);

        Intent i = getIntent();

       imageView3.setImageResource(android.R.drawable.stat_notify_error);
       nameTextView3.setText(i.getStringExtra("nameTextView3"));
       geoTextView3.setText(i.getStringExtra("geoTextView3"));
       addressTextView3.setText(i.getStringExtra("AddressTextView3"));

       Button button = findViewById(R.id.navigate_map_button);
       button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               // Creates an Intent that will load a map of San Francisco
                Uri gmmIntentUri = Uri.parse("geo:37.7749,-122.4194");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

    }
}
