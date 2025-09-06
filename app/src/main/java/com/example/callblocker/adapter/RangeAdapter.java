package com.example.callblocker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import com.example.callblocker.R;
import com.example.callblocker.model.NumberRange;
import com.example.callblocker.service.RangeManager;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class RangeAdapter extends BaseAdapter {
    private final Context context;
    private final List<NumberRange> ranges;
    private final OnDeleteClickListener deleteListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    @Override
    public int getCount() {
        return ranges.size();
    }

    @Override
    public Object getItem(int position) {
        return ranges.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.range_item, parent, false);
            holder = ViewHolder.builder()
                    .nameText((TextView) convertView.findViewById(R.id.rangeName))
                    .rangeText((TextView) convertView.findViewById(R.id.rangeNumbers))
                    .activeSwitch((Switch) convertView.findViewById(R.id.activeSwitch))
                    .deleteButton((Button) convertView.findViewById(R.id.deleteButton))
                    .build();
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        NumberRange range = ranges.get(position);

        holder.nameText.setText(range.getName());
        holder.rangeText.setText(String.format("%s - %s", range.getStartNumber(), range.getEndNumber()));
        holder.activeSwitch.setChecked(range.isActive());

        holder.activeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            range.setActive(isChecked);
            // Sauvegarder les changements
            RangeManager rangeManager = new RangeManager(context);
            rangeManager.saveRanges(ranges);
        });

        holder.deleteButton.setOnClickListener(v -> deleteListener.onDeleteClick(position));

        return convertView;
    }

    @Builder
    @Data
    private static class ViewHolder {
        private TextView nameText;
        private TextView rangeText;
        private Switch activeSwitch;
        private Button deleteButton;
    }
}