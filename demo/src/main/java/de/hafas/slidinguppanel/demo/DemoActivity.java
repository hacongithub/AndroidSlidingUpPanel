package de.hafas.slidinguppanel.demo;

import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hafas.slidinguppanel.SlidingUpPanelLayout;
import de.hafas.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;
import de.hafas.slidinguppanel.SlidingUpPanelLayout.PanelState;

public class DemoActivity extends AppCompatActivity {
    private static final String TAG = "DemoActivity";

    private SlidingUpPanelLayout mLayout;
    private List<String> list;
    private ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        setSupportActionBar((Toolbar) findViewById(R.id.main_toolbar));

        RecyclerView lv = findViewById(R.id.list);

        list = new ArrayList<>(Arrays.asList(
                "This",
                "Is",
                "An",
                "Example",
                "ListView",
                "That",
                "You",
                "Can",
                "Scroll",
                ".",
                "It",
                "Shows",
                "How",
                "Any",
                "Scrollable",
                "View",
                "Can",
                "Be",
                "Included",
                "As",
                "A",
                "Child",
                "Of",
                "SlidingUpPanelLayout"
        ));

        // This is the array adapter, it takes the context of the activity as a
        // first parameter, the type of list view as a second parameter and your
        // array as a third parameter.
        arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                list);

        lv.setAdapter(new SimpleAdapter(list));
        lv.setLayoutManager(new LinearLayoutManager(this));

        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        mLayout.addPanelSlideListener(new PanelSlideListener() {
            @Override
            public void onPanelSlide(@NonNull View panel, float slideOffset) {
                Log.i(TAG, "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelStateChanged(@NonNull View panel, @NonNull PanelState previousState, @NonNull PanelState newState) {
                Log.i(TAG, "onPanelStateChanged " + newState);
            }
        });
        mLayout.setFadeOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mLayout.setPanelState(PanelState.COLLAPSED);
            }
        });

        View footer = findViewById(R.id.footer);
        footer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DemoActivity.this, "onFooterClick", Toast.LENGTH_SHORT).show();
            }
        });

        TextView t = findViewById(R.id.name);
        t.setText(Html.fromHtml(getString(R.string.hello)));
        Button f = findViewById(R.id.follow);
        f.setText(getString(R.string.button));
        f.setMovementMethod(LinkMovementMethod.getInstance());
        f.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                View header = findViewById(R.id.header_view);
                final int oldHeight = header.getLayoutParams().height;
                final int initialHeight = v.getResources().getDimensionPixelSize(R.dimen.header_initial_height);
                final int expandedHeight = v.getResources().getDimensionPixelSize(R.dimen.header_expanded_height);
                if (oldHeight == initialHeight)
                    header.getLayoutParams().height = expandedHeight;
                else
                    header.getLayoutParams().height = initialHeight;
                header.requestLayout();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.demo, menu);
        MenuItem item = menu.findItem(R.id.action_toggle);
        if (mLayout != null) {
            if (mLayout.getPanelState() == PanelState.HIDDEN) {
                item.setTitle(R.string.action_show);
            } else {
                item.setTitle(R.string.action_hide);
            }
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_toggle: {
                if (mLayout != null) {
                    if (mLayout.getPanelState() != PanelState.HIDDEN) {
                        mLayout.setPanelState(PanelState.HIDDEN);
                        item.setTitle(R.string.action_show);
                    } else {
                        mLayout.setPanelState(PanelState.COLLAPSED);
                        item.setTitle(R.string.action_hide);
                    }
                }
                return true;
            }
            case R.id.action_anchor: {
                if (mLayout != null) {
                    if (mLayout.getAnchorPoint() == 1.0f) {
                        mLayout.setAnchorPoint(0.7f);
                        mLayout.setPanelState(PanelState.ANCHORED);
                        item.setTitle(R.string.action_anchor_disable);
                    } else {
                        mLayout.setAnchorPoint(1.0f);
                        mLayout.setPanelState(PanelState.COLLAPSED);
                        item.setTitle(R.string.action_anchor_enable);
                    }
                }
                return true;
            }
            case R.id.action_auto_height: {
                if (mLayout != null) {
                    if (mLayout.getPanelAutoHeightEnabled()) {
                        mLayout.setPanelHeight(getResources().getDimensionPixelSize(R.dimen.header_initial_height));
                        item.setTitle(R.string.action_auto_height_enable);
                    } else {
                        mLayout.setPanelHeight(SlidingUpPanelLayout.PANEL_HEIGHT_AUTO);
                        item.setTitle(R.string.action_auto_height_disable);
                    }
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mLayout != null &&
                (mLayout.getPanelState() == PanelState.EXPANDED || mLayout.getPanelState() == PanelState.ANCHORED)) {
            mLayout.setPanelState(PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    static class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.ViewHolder> {
        private final List<String> items;

        SimpleAdapter(List<String> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent,false));
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            ((TextView) holder.itemView).setText(items.get(position));
            holder.itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(), "onItemClick " + holder.getAdapterPosition(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            ViewHolder(@NonNull View itemView) {
                super(itemView);
            }
        }
    }
}
