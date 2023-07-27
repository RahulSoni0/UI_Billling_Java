package com.blockermode.focusmode.uibilllingjava;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.blockermode.focusmode.uibilllingjava.billingUtils.BillingHelper;
import com.blockermode.focusmode.uibilllingjava.model.PremiumSelectionDetail;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    Activity activity;
    PremiumAdapter adapter;
    ArrayList<PremiumSelectionDetail> list = new ArrayList<>();
    PremiumSelectionDetail premiumSelectionDetail;
    private BillingHelper billingHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;

        findViewById(R.id.button_close).setOnClickListener(view -> onBackPressed());
        recyclerViewSetup();

        findViewById(R.id.buttonBuy).setOnClickListener(view -> {

            if (adapter.getSelected() == null) {
                Toast.makeText(activity, "No Selection", Toast.LENGTH_SHORT).show();
                return;
            }

            premiumSelectionDetail = adapter.getSelected();


            //call buy here...
            ArrayList<String> skuIds = new ArrayList<>();
            for (PremiumSelectionDetail p : list) {
                skuIds.add(p.getProductId());
            }
            initBillingClient(skuIds);
            startPayment(billingHelper, premiumSelectionDetail.getProductId());

        });
    }

    private void initBillingClient(ArrayList<String> skuIds) {
        billingHelper = new BillingHelper(MainActivity.this, skuIds, "", purchaseUpdateListener);
    }

    private void setData(){

    }

    private void startPayment(BillingHelper helper, String skuId){
        helper.startPayment( this, skuId, BillingClient.ProductType.SUBS);
    }

    private void recyclerViewSetup() {

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list.add(new PremiumSelectionDetail("BlockerMode Monthly Subscription", "₹120 / Monthly", "bm_monthly_subscription", "bm-monthly-subscription", "subscription"));

        list.add(new PremiumSelectionDetail("BlockerMode Quarterly Subscription", "₹350 / Quarterly", "bm_quarterly_subscription", "bm-quarterly-subscription", "subscription"));

        list.add(new PremiumSelectionDetail("BlockerMode Yearly Subscription", "₹1,200 / Yearly", "bm_yearly_subscription", "bm-yearly-subscription", "subscription"));

        list.add(new PremiumSelectionDetail("BlockerMode One Time Purchase Subscription", "₹5,500 / One Time Purchase", "bm_one_time_purchase_subscription", "purchase", "purchase"));

        adapter = new PremiumAdapter(activity, list);
        recyclerView.setAdapter(adapter);

    }


    private final PurchasesUpdatedListener purchaseUpdateListener = (billingResult, purchases) -> {
        if (billingHelper.paymentInit) {
            billingHelper.paymentInit = false;
            try {
                //payment success
            } catch (Exception e) {
                Log.e("Exception: ", e.getLocalizedMessage());
            }
        }
    };
}