package li.com.mmap;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class RV2Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<Row> rowList;
    Context context;

    public RV2Adapter(List<Row> rows, Context context) {
        this.rowList = rows;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv2_item_details, parent, false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        ViewHolder viewHolder = (ViewHolder)holder;
        Row row = rowList.get(position);

        //to be used
      //  Picasso.get().load("http://i.imgur.com/DvpvklR.png").into(imageView);

        viewHolder.imageView2.setImageResource(row.getImageId());
        viewHolder.nameTextView2.setText(row.getName());
        viewHolder.geoTextView2.setText(row.getGeo().toString());
        viewHolder.addressTextView2.setText(row.getAddress());
    }

    @Override
    public int getItemCount() {
        return rowList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        CardView cv2;
        ImageView imageView2;
        TextView nameTextView2, geoTextView2, addressTextView2;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            cv2 = (CardView)view.findViewById(R.id.cv);
            imageView2 = view.findViewById(R.id.imageView2);
            nameTextView2 = view.findViewById(R.id.textViewName2);
            geoTextView2 = view.findViewById(R.id.textViewGeo2);
            addressTextView2 = view.findViewById(R.id.textViewAddress2);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Row row = rowList.get(position);

            Intent intent = new Intent(v.getContext(), SingleItemDetailsActivity.class);

            intent.putExtra("imageView3", row.getImageId());
            intent.putExtra("nameTextView3", row.getName());
            intent.putExtra("geoTextView3", row.getGeo().toString());
            intent.putExtra("addressTextView3", row.getAddress());

            v.getContext().startActivity(intent);
        }
    }


}