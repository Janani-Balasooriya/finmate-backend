package com.finmate.finmate_backend.insights.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InsightsCacheInvalidationService {

    private final CacheManager cacheManager;

    public void invalidateInsightsCache(Long familyId) {
        if (cacheManager.getCache("insights") != null) {
            cacheManager.getCache("insights").evict(familyId);
        }
    }

    public void invalidateAllInsightsCache() {
        if (cacheManager.getCache("insights") != null) {
            cacheManager.getCache("insights").clear();
        }
    }
}