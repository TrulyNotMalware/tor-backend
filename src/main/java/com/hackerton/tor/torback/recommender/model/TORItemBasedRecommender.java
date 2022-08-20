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

import java.util.List;

@Slf4j
@AllArgsConstructor
@Component("TORItemBasedRecommender")
public class TORItemBasedRecommender {
    private ItemSimilarity itemSimilarity;
    private ItemBasedRecommender itemBasedRecommender;

    //Constructor
    TORItemBasedRecommender(){
        log.debug("create New ItemBasedRecommender");
        DataModel dataModel = new DataModels().getDataModel();
        try{
            this.itemSimilarity = new PearsonCorrelationSimilarity(dataModel);
        }catch (TasteException e) {
            e.printStackTrace();
        }
        this.itemBasedRecommender = new GenericItemBasedRecommender(dataModel,this.itemSimilarity);
    }

    /**
     * Three default functions insert.
     */

    public List<RecommendedItem> getRecommendedItemsByUserId(long userID, int size) {
        List<RecommendedItem> recommendations = null;
        log.debug("getRecommendedItemsByUserId : {}",userID);
        try {
            recommendations = this.itemBasedRecommender.recommend(userID, size);
        } catch (Exception e) {
            e.printStackTrace();
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
}
