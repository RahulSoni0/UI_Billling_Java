package com.blockermode.focusmode.uibilllingjava.billingUtils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillingHelper implements BillingClientStateListener, SkuDetailsResponseListener {

    private static final String TAG = "in-app-billing";
    private Context context;
    private ArrayList<String> skuList;
    private String subscriptionPackage;
    private PurchasesUpdatedListener purchasesUpdatedListener;
    private Map<String, SkuDetails> skus = new HashMap<>();
    public boolean paymentInit = false;
    private BillingClient billingClient;

    public BillingHelper(Context context, ArrayList<String> skuList, String subscriptionPackage,
                         PurchasesUpdatedListener purchasesUpdatedListener) {
        this.context = context;
        this.skuList = new ArrayList<>(skuList);
        this.subscriptionPackage = subscriptionPackage;
        this.purchasesUpdatedListener = purchasesUpdatedListener;
        billingClient = BillingClient.newBuilder(context)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();
        billingClient.startConnection(this);
        for (String sku : skuList) {
            if (sku != null) {
                this.skuList.add(sku);
            }
        }
    }

    public Pair<Boolean, Integer> startPayment(Activity activity, String forSKU, String skuType) {
        paymentInit = true;
        SkuDetails sku = skus.get(forSKU);
        if (sku == null) {
            Log.d(TAG, "SKU DOESN'T EXIST or billing not initialized");
            return new Pair<>(false, BillingClient.BillingResponseCode.ERROR);
        }

        List<String> subscribedPackageList = null;
        if (subscriptionPackage != null) {
            subscribedPackageList = new ArrayList<>();
            String[] packages = subscriptionPackage.split(":");
            for (String pack : packages) {
                subscribedPackageList.add(pack.trim());
            }
        }

        BillingFlowParams.Builder billingFlow = BillingFlowParams.newBuilder().setSkuDetails(sku);
        BillingFlowParams.SubscriptionUpdateParams.Builder subscriptionUpdateParams = BillingFlowParams.SubscriptionUpdateParams.newBuilder();

        if (subscribedPackageList != null && skuType.equals(BillingClient.SkuType.SUBS)) {
            billingFlow.setSubscriptionUpdateParams(subscriptionUpdateParams
                    .setOldSkuPurchaseToken(subscribedPackageList.get(1))
                    .setReplaceSkusProrationMode(BillingFlowParams.ProrationMode.IMMEDIATE_AND_CHARGE_FULL_PRICE)
                    .build());
        }
        BillingResult billingResult = billingClient.launchBillingFlow(activity, billingFlow.build());
        return billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                ? new Pair<>(true, billingResult.getResponseCode())
                : new Pair<>(false, billingResult.getResponseCode());
    }

    private void queryAndCacheSkuDetails() {
        if (skuList != null) {
            billingClient.querySkuDetailsAsync(SkuDetailsParams.newBuilder()
                            .setSkusList(skuList)
                            .setType(BillingClient.SkuType.INAPP)
                            .build(),
                    this);
            billingClient.querySkuDetailsAsync(SkuDetailsParams.newBuilder()
                            .setSkusList(skuList)
                            .setType(BillingClient.SkuType.SUBS)
                            .build(),
                    this);
        }
    }

    @Override
    public void onBillingSetupFinished(BillingResult billingResult) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            queryAndCacheSkuDetails();
        } else {
            Log.d(TAG, "onBillingSetupFinished code : " + billingResult.getResponseCode() + " " + billingResult.getDebugMessage());
        }
    }

    @Override
    public void onBillingServiceDisconnected() {
        Log.d(TAG, "onBillingServiceDisconnected");
    }

    @Override
    public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> sdL) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            List<SkuDetails> skuDetailsList = sdL;
            if (skuDetailsList != null) {
                for (SkuDetails sku : skuDetailsList) {
                    skus.put(sku.getSku(), sku);
                }
                //data ko store krlo
            }
        } else {
            Log.d(TAG, "SkuDetailsResponse code : " + billingResult.getResponseCode() + " " + billingResult.getDebugMessage());
        }
    }
}