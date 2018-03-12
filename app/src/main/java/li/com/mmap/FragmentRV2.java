package li.com.mmap;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import static li.com.mmap.DividerItemDecoration.VERTICAL_LIST;

public class FragmentRV2 extends Fragment {

    List<Row> rowList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        RecyclerView rv = (RecyclerView) inflater.inflate(R.layout.fragment_rv2, container, false);

        rv.setLayoutManager(new LinearLayoutManager(rv.getContext()));
        rv.addItemDecoration(new DividerItemDecoration(getActivity(), VERTICAL_LIST));

        rowList = getDatabaseArray();

        rv.setAdapter(new RV2Adapter(rowList, getContext()));

        return rv;
    }

    private List<Row> getDatabaseArray(){

        //no network
        //get data from database
        //query whole database

        SQLiteDatabaseManager sQLiteDatabaseManager= new SQLiteDatabaseManager(getContext());

        rowList = sQLiteDatabaseManager.getData();

        return rowList;
    }
}
