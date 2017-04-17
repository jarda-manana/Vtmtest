package org.xctrack.jarda.vtmtest;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.view.View;

import org.oscim.android.MapPreferences;
import org.oscim.android.MapView;
import org.oscim.core.MapPosition;
import org.oscim.core.Tile;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.map.Map;
import org.oscim.renderer.BitmapRenderer;
import org.oscim.renderer.GLViewport;
import org.oscim.scalebar.DefaultMapScaleBar;
import org.oscim.scalebar.ImperialUnitAdapter;
import org.oscim.scalebar.MapScaleBar;
import org.oscim.scalebar.MapScaleBarLayer;
import org.oscim.scalebar.MetricUnitAdapter;
import org.oscim.theme.VtmThemes;
import org.oscim.tiling.source.mapfile.MapFileTileSource;
import org.oscim.tiling.source.mapfile.MapInfo;

/**
 * Created by jarda on 16.4.17.
 */

public class MainView extends View {
    long _renderRepaintIntervalMS = 1000;
    MapView mMapView;
    Map mMap;
    MapPreferences mPrefs;
    private DefaultMapScaleBar mMapScaleBar;
    MapPosition mPos = new MapPosition();


    public class Invalidator implements Runnable {
        public void stop() {
            MainView.this.removeCallbacks(this);
        }

        public void reschedule() {
            stop();
            schedule();
        }

        private void schedule() {
            MainView.this.postDelayed(this, _renderRepaintIntervalMS - (System.currentTimeMillis() % _renderRepaintIntervalMS));
        }

        @Override
        public void run() {
            schedule();
            MainView.this.invalidate();
        }
    }

    public Invalidator invalidator;
    Paint p;

    public MainView(MainActivity activity) {
        super(activity);
        setId(R.id.mainViewReservedId);
        setFocusable(true);
        invalidator = new Invalidator();
        p = new Paint();
        p.setColor(Color.RED);

        Tile.SIZE = Tile.calculateTileSize(getResources().getDisplayMetrics().scaledDensity);


        mMapView = new MapView(activity);
        mMap = mMapView.map();
        mPrefs = new MapPreferences("aaa", activity);


        MapFileTileSource tileSource = new MapFileTileSource();
        tileSource.setPreferredLanguage("en");
        String file = Environment.getExternalStorageDirectory() + "/germany.map";
        if (tileSource.setMapFile(file)) {

            VectorTileLayer l = mMap.setBaseMap(tileSource);
            mMap.setTheme(VtmThemes.DEFAULT);

            mMap.layers().add(new BuildingLayer(mMap, l));
            mMap.layers().add(new LabelLayer(mMap, l));

            mMapScaleBar = new DefaultMapScaleBar(mMap);
            mMapScaleBar.setScaleBarMode(DefaultMapScaleBar.ScaleBarMode.BOTH);
            mMapScaleBar.setDistanceUnitAdapter(MetricUnitAdapter.INSTANCE);
            mMapScaleBar.setSecondaryDistanceUnitAdapter(ImperialUnitAdapter.INSTANCE);
            mMapScaleBar.setScaleBarPosition(MapScaleBar.ScaleBarPosition.BOTTOM_LEFT);

            MapScaleBarLayer mapScaleBarLayer = new MapScaleBarLayer(mMap, mMapScaleBar);
            BitmapRenderer renderer = mapScaleBarLayer.getRenderer();
            renderer.setPosition(GLViewport.Position.BOTTOM_LEFT);
            renderer.setOffset(5 * getResources().getDisplayMetrics().density, 0);
            mMap.layers().add(mapScaleBarLayer);

            MapInfo info = tileSource.getMapInfo();
            mPos.setByBoundingBox(info.boundingBox, Tile.SIZE * 4, Tile.SIZE * 4);
            mMap.setMapPosition(mPos);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int widthSpec = View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(400, View.MeasureSpec.EXACTLY);

        mMapView.measure(widthSpec, heightSpec);

//        mMapView.layout(0, 0, x2-x1, y2-y1);
        mMapView.layout(0, 0, 400, 400);
        try {
            canvas.save();

            canvas.clipRect(200, 200, 600, 600);
            canvas.translate(200, 200);
            mMapView.draw(canvas);

        } finally {
            canvas.restore();
        }
        canvas.drawRect(20, 20, 100, 100 + 100 * ((System.currentTimeMillis() / _renderRepaintIntervalMS) % 2), p);

        invalidator.reschedule();
    }

    protected void onResume() {
        mPrefs.load(mMapView.map());
        mMapView.onResume();
    }

    protected void onPause() {
        mPrefs.save(mMapView.map());
        mMapView.onPause();

    }

    protected void onDestroy() {
        mMapView.onDestroy();
    }
}
