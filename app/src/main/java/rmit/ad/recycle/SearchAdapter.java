package rmit.ad.recycle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> implements Filterable {
    public interface OnItemClickListener {
        void onItemClick(String item);
    }

    private List<String> items;
    private List<String> allItems;
    private SearchAdapter.OnItemClickListener onItemClickListener;
    SearchAdapter(List<String> items, SearchAdapter.OnItemClickListener onItemClickListener) {
        this.items = items;
        this.allItems = new ArrayList<>(items);
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search, parent, false);
        return new SearchAdapter.SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, final int position) {
        holder.textView.setText(items.get(position));

        holder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onItemClick(items.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public Filter getFilter() {
        return itemFilter;
    }

    class SearchViewHolder extends RecyclerView.ViewHolder {

        TextView textView;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.name);
        }
    }

    private Filter itemFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<String> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(allItems);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (String item: allItems) {
                    if (item.toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            items = new ArrayList<>();
            items.addAll( (List)results.values);
            notifyDataSetChanged();
        }
    };
}
