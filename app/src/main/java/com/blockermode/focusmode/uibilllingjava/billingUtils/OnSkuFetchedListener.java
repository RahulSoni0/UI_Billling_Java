package com.blockermode.focusmode.uibilllingjava.billingUtils;

import com.android.billingclient.api.SkuDetails;

import java.util.List;

public interface OnSkuFetchedListener {
    void onSkuFetched(List<SkuDetails> list);
}
