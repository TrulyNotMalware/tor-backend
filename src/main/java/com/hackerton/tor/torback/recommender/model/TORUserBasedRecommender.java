package com.hackerton.tor.torback.recommender.model;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Component("TORUserBasedRecommender")
public class TORUserBasedRecommender {

    private UserSimilarity userSimilarity;
    private UserSimilarity presetUserSimilarity;
    private UserNeighborhood neighborhood;
    private UserNeighborhood presetNeighborhood;
    private UserBasedRecommender recommender;
    private UserBasedRecommender presetRecommender;

    //Constructor
    TORUserBasedRecommender() {
        log.debug("create New UserBasedRecommender");
        DataModel dataModel = new DataModels().getDataModel();
        DataModel presetDataModel = new DataModels().getPresetDataModel();
        try {
            this.userSimilarity = new PearsonCorrelationSimilarity(dataModel);
            this.presetUserSimilarity = new PearsonCorrelationSimilarity(presetDataModel);

            this.neighborhood = new NearestNUserNeighborhood(6, this.userSimilarity, dataModel);
            this.presetNeighborhood = new NearestNUserNeighborhood(6, this.presetUserSimilarity, presetDataModel);
        } catch (TasteException e) {
            e.printStackTrace();
        }
        this.recommender = new GenericUserBasedRecommender(dataModel, this.neighborhood, this.userSimilarity);
        this.presetRecommender = new GenericUserBasedRecommender(presetDataModel, this.presetNeighborhood, this.presetUserSimilarity);
    }

    /**
     * Three default functions insert.
     * Version 1) Product Recommend,
     * Version 2) Preset Recommend
     */

    public List<RecommendedItem> getRecommendedItemsByUserId(long userId, int size) {
        List<RecommendedItem> recommendations = null;
        log.debug("getRecommendedItemsByUserId - User : {}",userId);
        try {
            recommendations = this.recommender.recommend(userId, size);
        } catch (TasteException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
        return recommendations;
    }

    public void refresh() {
        this.recommender.refresh(null);
    }

    public long[] getUsersMostSimilar(long userId,int size) {
        long[] mostSimilarUsers = null;
        try {
            mostSimilarUsers = this.recommender.mostSimilarUserIDs(userId, size);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  mostSimilarUsers;
    }

    public List<RecommendedItem> getPresetRecommendedItemsByUserId(long userId, int size) {
        List<RecommendedItem> recommendations = null;
        log.debug("getRecommendedPresetItemByUserId - User : {}",userId);
        try {
            recommendations = this.presetRecommender.recommend(userId, size);
        } catch (TasteException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
        log.debug("Size : {}",recommendations.size());
        for( RecommendedItem item : recommendations){
            log.debug("Recom : {}",item.getItemID());
        }
        return recommendations;
    }

    public void presetRefresh() {
        this.presetRecommender.refresh(null);
    }

    public long[] getPresetUsersMostSimilar(long userId,int size) {
        long[] mostSimilarUsers = null;
        try {
            mostSimilarUsers = this.presetRecommender.mostSimilarUserIDs(userId, size);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  mostSimilarUsers;
    }
}
