package com.amsu.test.wifiTramit;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.amsu.test.R;

import java.util.List;

/**
 * Created by HP on 2017/4/26.
 */

public class FileListAdapter  extends BaseAdapter {
    private List<String> fileNameList;
    private Context context;

    public FileListAdapter(List<String> fileNameList, Context context) {
        this.fileNameList = fileNameList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return fileNameList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final String fileName = fileNameList.get(position);

        View inflate = View.inflate(context, R.layout.item, null);
        TextView tv_filename = (TextView) inflate.findViewById(R.id.tv_filename);
        tv_filename.setText(fileName);
        return inflate;
    }
}
