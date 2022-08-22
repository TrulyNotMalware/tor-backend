package com.hackerton.tor.torback.recommender.model;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.springframework.stereotype.Component;

@Slf4j
@AllArgsConstructor
@Component("TORUserBasedRecommender")
public class TORUserBasedRecommender {

    private UserSimilarity itemSimilarity;
    private UserSimilarity presetItemSimilarity;
    private UserNeighborhood neighborhood;
    private UserNeighborhood presetNeighborhood;

    TORUserBasedRecommender() {
        log.debug("create New UserBasedRecommender");
        DataModel dataModel = new DataModels().getDataModel();
        DataModel presetDataModel = new DataModels().getPresetDataModel();
        try {
            this.itemSimilarity = new PearsonCorrelationSimilarity(dataModel);
            this.presetItemSimilarity = new PearsonCorrelationSimilarity(presetDataModel);
            neighborhood = new NearestNUserNeighborhood(6, this.itemSimilarity, dataModel);
        } catch (TasteException e) {
            e.printStackTrace();
        }
//        recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
    }

}
