package com.hackerton.tor.torback.recommender.model;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Component("TORItemBasedRecommender")
public class TORItemBasedRecommender {
    private ItemSimilarity itemSimilarity;
    private ItemSimilarity presetItemSimilarity;
    private ItemBasedRecommender itemBasedRecommender;
    private ItemBasedRecommender presetItemBasedRecommender;

    //Constructor
    TORItemBasedRecommender(){
        log.debug("create New ItemBasedRecommender");
        DataModel dataModel = new DataModels().getDataModel();
        DataModel presetDataModel = new DataModels().getPresetDataModel();
        try{
            this.itemSimilarity = new PearsonCorrelationSimilarity(dataModel);
            this.presetItemSimilarity = new PearsonCorrelationSimilarity(presetDataModel);
        }catch (TasteException e) {
            e.printStackTrace();
        }
        this.itemBasedRecommender = new GenericItemBasedRecommender(dataModel,this.itemSimilarity);
        this.presetItemBasedRecommender = new GenericItemBasedRecommender(presetDataModel,this.presetItemSimilarity);
    }

    /**
     * Three default functions insert.
     * Version 1) Product Recommend,
     * Version 2) Preset Recommend
     */

    public List<RecommendedItem> getRecommendedItemsByUserId(long userID, int size) {
        List<RecommendedItem> recommendations = null;
        log.debug("getRecommendedItemsByUserId - Item : {}",userID);
        try {
            recommendations = this.itemBasedRecommender.recommend(userID, size);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
        log.debug("Size : {}",recommendations.size());
        for( RecommendedItem item : recommendations){
            log.debug("Recom : {}",item.getItemID());
        }
        return recommendations;
    }

    public void refresh() {
        this.itemBasedRecommender.refresh(null);
    }

    public List<RecommendedItem> getItemsMostSimilar(long itemId,int size) {
        List<RecommendedItem> recommendations = null;
        try {
            recommendations = this.itemBasedRecommender.mostSimilarItems(itemId, size);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  recommendations;
    }


    public List<RecommendedItem> getRecommendedPresetItemByUserId(long userId, int size){
        List<RecommendedItem> recommendations = null;
        log.debug("getRecommendedPresetItemByUserId - Item : {}",userId);
        try {
            recommendations = this.presetItemBasedRecommender.recommend(userId, size);
        } catch (Exception e) {
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
        this.presetItemBasedRecommender.refresh(null);
    }

    public List<RecommendedItem> getPresetItemsMostSimilar(long itemId,int size) {
        List<RecommendedItem> recommendations = null;
        try {
            recommendations = this.presetItemBasedRecommender.mostSimilarItems(itemId, size);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return  recommendations;
    }
}
