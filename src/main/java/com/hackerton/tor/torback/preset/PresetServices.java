package com.hackerton.tor.torback.preset;

import com.hackerton.tor.torback.entity.*;
import com.hackerton.tor.torback.repository.PresetRepository;
import com.hackerton.tor.torback.repository.PurchaseRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@Service
public class PresetServices {

    PresetRepository presetRepository;
    PurchaseRepository purchaseRepository;
    DatabaseClient databaseClient;

    public Flux<Preset> getPresetListTop20(){
        return this.presetRepository.getPresetListByRecommend()
                .doOnError(error ->log.trace(error.getMessage())).log("getPreset Ranking");
    }

    public Flux<Preset> getMyPresetLists(@NotNull String userId){
        return this.presetRepository.getMyPresetLists(userId)
                .doOnError(error -> log.trace(error.getMessage())).log("get My Presets");
    }

    public Mono<Preset> getPresetByPresetId(@NotNull int presetId){
        return this.presetRepository.getPresetById(presetId)
                .doOnError(error -> log.trace(error.getMessage()));
    }

    public Mono<Preset_preferences> insertPresetPreferences(@NotNull long userNumber,
                                                            @NotNull long presetId,
                                                            @NotNull float preference){
        return this.presetRepository.insertPresetPreference(userNumber, presetId, preference)
                .doOnError(error -> log.trace(error.getMessage()));
    }

    public Mono<Preset> getPresetByPresetName(@NotNull String presetName){
        return this.presetRepository.getPresetByName(presetName)
                .doOnError(error -> log.trace(error.getMessage()));
    }

    public Mono<Preset> updateRecommendByPresetId(@NotNull int presetId,
                                                  @NotNull String userId){
        return this.databaseClient.sql("SELECT COALESCE(SUM(recommend),0) AS recommend FROM user_preset_binding\n" +
                "WHERE userId = '"+userId+"'\n" +
                "AND presetId = "+presetId+";\n").fetch().one()
                .flatMap(stringObjectMap -> {
                    if(Integer.parseInt(String.valueOf(stringObjectMap.get("recommend")))>0) return Mono.empty();
                    else return this.databaseClient.sql("SELECT COUNT(*) AS exist FROM preset\n" +
                            "WHERE producer = '"+userId+"'\n" +
                            "AND presetId = "+presetId+";\n").fetch().one()
                            .flatMap(stringObjectMap1 -> {
                                if(Integer.parseInt(String.valueOf(stringObjectMap1.get("exist")))>0) return Mono.empty();
                                else return this.presetRepository.updatePresetRecommend(presetId,userId)
                                        .doOnError(error -> log.trace(error.getMessage()));
                            });
                });
    }

    public Flux<User_preset_binding> getPurchasedHistory(@NotNull String userId){
        return this.presetRepository.getPresetPurchasedHistory(userId)
                .doOnError(error -> log.trace(error.getMessage()));
    }

    public Mono<List<Purchase_history>> updatePurchaseHistory(
            @NotNull String userId,
            @NotNull long presetId,
            Map<String,Integer> items){
        long buyCount = 1;
        return this.presetRepository.updateBuyCount(presetId).flatMap(preset -> {
            log.debug(String.valueOf(preset.getPresetId()));
            return this.presetRepository.insertNewUserPresetBinding(userId,presetId,buyCount)
                    .flatMap(user_preset_binding -> Mono.just(items.keySet()).flatMapMany(Flux::fromIterable)
                            .flatMap(key -> this.purchaseRepository.insertNewPurchaseHistory(userId,key,items.get(key))).collectList());
        });
    }

    public Mono<List<Preset_detail>> createPreset(
                @NotNull String presetName,
                String presetContent,
                @NotNull String presetCategoryName,
                @NotNull String producer,
                Map<String,String> items
    ){
            return this.presetRepository.insertNewPreset(presetName,presetContent,presetCategoryName,producer)
                    .flatMap(preset -> {
                        long presetId = preset.getPresetId();
                        return Mono.just(items.keySet()).flatMapMany(Flux::fromIterable)
                                .flatMap(key -> {
                                    String categoryName = items.get(key);
                                    return this.presetRepository.insertPresetDetail(presetId,categoryName,Integer.parseInt(key));
                                }).collectList();
                    });
    }

    public Mono<Double> getEvalPresetScores(@NotNull String userId, @NotNull int presetId){
        //Final Return

        return Mono.zip( // Collectable set's eval functions.
                getPresetDetailNumberScore(presetId).log("getPresetDetailNumberScore"),
                getPresetRatioScore(presetId).log("getPresetRatioScore"),
                getPresetRecentlyPopularityScore(presetId).log("getPresetRecentlyPopularityScore"),
                getProductPopularityScore(presetId),
                getPresetDiversityScore(presetId))
                .flatMap(objects -> Mono.zip(getDoRecommendScore(userId, presetId),
                        getSimilarBuyPreferenceScore(userId, presetId),
                        getSimilarRecommendPreferenceScore(userId, presetId),
                        getRelativeWithPurchasedHistoryScore(userId, presetId))
                        .flatMap(objects2 ->
                                Mono.just(
                                        objects.getT1()+objects.getT2()+objects.getT3()+objects.getT4()+objects.getT5()
                        +objects2.getT1()+objects2.getT2()+objects2.getT3()+ objects2.getT4())));
    }

    // 모음집 평가 함수 1번 : 모음집 내에 있는 제품들이 다른 사용자들에게 얼마나 인기있는지 평가
    public Mono<Double> getProductPopularityScore ( int presetId ){
//        double score = 0; // 모음집의 점수 = 15 * sumProductScore / presetDetailNum
//        int presetDetailNum = 1; // 모음집 내 제품들의 개수
//        double sumProductScore = 0; // 모음집 내 제품 점수의 합
        return this.databaseClient.sql("SELECT count(*) as number FROM preset_detail\n" +
                "WHERE presetId = " + presetId).fetch().one()
                .flatMap(numberObjects -> {
                    log.debug("getProductPopularityScore_1 : {}",numberObjects.size());
                    int presetDetailNum = Integer.parseInt(String.valueOf(numberObjects.get("number"))); // 모음집 내 제품들의 개수
                    return this.databaseClient.sql("SELECT productId FROM preset_detail\n" +
                            "WHERE presetId = " + presetId)
                            .fetch().all()
                            .flatMap(presetField -> {
                                int productId = Integer.parseInt(String.valueOf(presetField.get("productId")));
                                return getProductPopularity(productId);
                            })
                            .collectList().log("collectDoubles")
                            .flatMap(doubles -> {
                                double sumProductScore = doubles.stream().reduce(0D,Double::sum);// 모음집 내 제품 점수의 합
                                return Mono.just(sumProductScore);
                            });
                        }).switchIfEmpty(Mono.defer(() -> Mono.just(0.0D)));
    }

    public Mono<Double> getProductPopularity ( int productId ){
        return this.databaseClient.sql("SELECT COUNT(*) AS number FROM purchase_history ph\n" +
                "JOIN product p ON ph.productId = p.productId\n" +
                "WHERE p.categoryName IN (\n" +
                "        SELECT categoryName\n" +
                "        FROM product tmpP\n" +
                "        WHERE tmpP.productId = " + productId + "\n" +
                ");").fetch().one()
                .flatMap(numberObjects -> {
                    int purchasedProductNum = Integer.parseInt(String.valueOf(numberObjects.get("number"))); // 제품과 같은 카테고리의 제품들 중, 구매 이력이 있는 제품들의 갯수
                    return this.databaseClient.sql("SELECT ranking\n" +
                            "FROM(\n" +
                            "    SELECT * ,(@rank := @rank + 1) ranking FROM (\n" +
                            "        SELECT ph.productId, SUM(count)\n" +
                            "        FROM purchase_history ph\n" +
                            "        JOIN product p on ph.productId = p.productId\n" +
                            "        WHERE p.categoryName IN (\n" +
                            "            SELECT categoryName\n" +
                            "            FROM product tmpP\n" +
                            "            WHERE tmpP.productId = 1\n" +
                            "        )\n" +
                            "        GROUP BY ph.productId\n" +
                            "        ORDER BY SUM(count) DESC\n" +
                            ") tb , (SELECT @rank := 0) tmp\n" +
                            "            )  tb2\n" +
                            "WHERE tb2.productId=" + productId + "\n").fetch().one()
                            .flatMap(rankObjects -> {
                                double score = 0.0; // 제품의 점수, 공식 = 1 - productRank/purchasedProductNum
                                int productRank = Integer.parseInt(String.valueOf(rankObjects.get("ranking")));
                                if(purchasedProductNum == 0 || productRank == 0){
                                    score = 0;
                                }
                                else{
                                    score = 1 - 1.0* productRank/purchasedProductNum;
                                }
                                return Mono.just(score);
                            });
                }).switchIfEmpty(Mono.defer(() -> Mono.just(0.0D)));
    }

    // 모음집 평가 함수 2번 : 인기 모음집(추천수 상위 10개)의 제품 가짓수와 비교하여, 갯수가 적절한지 평가
    public Mono<Double> getPresetDetailNumberScore( int presetId ){
        return this.databaseClient.sql("SELECT AVG(number) as avgNumber FROM (\n" +
                "    SELECT COUNT(*) AS number\n" +
                "    FROM preset_detail\n" +
                "    WHERE presetId in (\n" +
                "        SELECT presetId\n" +
                "        FROM (\n" +
                "                 SELECT presetId\n" +
                "                 FROM preset\n" +
                "                 ORDER BY recommend DESC\n" +
                "                 LIMIT 10\n" +
                "             ) as tmp1\n" +
                "    )\n" +
                "    GROUP BY presetId\n" +
                ") as tmp2").fetch().one()
                .flatMap(avgNumberObject -> {
                    double avgPresetDetailNumber = Double.parseDouble(String.valueOf(avgNumberObject.get("avgNumber")));
                    return this.databaseClient.sql("SELECT COUNT(*) as number FROM preset_detail\n" +
                            "WHERE presetId = " + presetId + "\n")
                            .fetch().one()
                            .flatMap(numberObject -> {
                                double presetDetailNumber = Double.parseDouble(String.valueOf(numberObject.get("number")));
                                double score = 0; // 모음집의 점수 = 10 * (1 - Math.abs(presetDetailNumber/avgPresetDetailNumber - 1))
                                score = 10 * (1 - Math.abs(presetDetailNumber/avgPresetDetailNumber - 1));
                                if(score < 0){
                                    score = 0;
                                }
                                return Mono.just(score);
                            });
                }).switchIfEmpty(Mono.defer(() -> Mono.just(0.0D)));
    }

    // 모음집 평가 함수 3번 : 조회수에 비해 사람들이 얼마나 추천,구매 했는지 평가
    public Mono<Double> getPresetRatioScore( int presetId ){
        double score = 0; // 모음집의 점수 = 5 * (recommendNumber/views + buyCount/recommendNumber);
        return this.presetRepository.getPresetById(presetId)
                .flatMap(preset -> Mono.just(5.0 * (1.0 * preset.getRecommend()/preset.getViews() + 1.0 * preset.getBuyCount()/preset.getViews())))
                .switchIfEmpty(Mono.defer(() -> Mono.just(0.0D)));
    }

    // 모음집 평가 함수 4번 : 2주 정도의 기간동안 판매량 평가
    public Mono<Double> getPresetRecentlyPopularityScore( int presetId ){
        return this.databaseClient.sql("SELECT ranking FROM (\n" +
                "                        SELECT *, (@rank := @rank + 1) AS ranking\n" +
                "                        FROM (\n" +
                "                                 SELECT presetId, buyCount, createdAt\n" +
                "                                 FROM user_preset_binding\n" +
                "                                 GROUP BY presetId\n" +
                "                             ) tmp1,\n" +
                "                             (SELECT @rank := 0) tmp2\n" +
                "                        WHERE createdAt <= NOW() AND createdAt >= DATE_SUB(NOW(), INTERVAL 2 WEEK )\n" +
                "                        ORDER BY buyCount DESC\n" +
                "                    ) tmp3\n" +
                "WHERE presetId = " + presetId + "\n").fetch().one()
                .flatMap(rankingObject -> {
                    double presetRank = Double.parseDouble(String.valueOf(rankingObject.get("ranking")));
                    return this.databaseClient.sql("SELECT COUNT(DISTINCT presetId) AS presetNumber FROM user_preset_binding\n" +
                            "WHERE createdAt <= NOW() AND createdAt >= DATE_SUB(NOW(), INTERVAL 2 WEEK )").fetch().one()
                            .flatMap(countObject -> {
                                int presetNumber = Integer.parseInt(String.valueOf(countObject.get("presetNumber")));
                                double score = 0; // 모음집 점수 = 10*(1- presetRank/presetNumber)
                                if(presetRank == 0 || presetNumber == 0){
                                    score = 0.0;
                                }
                                else{
                                    score = 10*(1- 1.0*presetRank/presetNumber);
                                }
                                return Mono.just(score);
                            });
                }).switchIfEmpty(Mono.defer(() -> Mono.just(0.0D)));
    }

    // 모음집 평가 함수 5번 : 모음집 내 제품의 다양성 평가
    public Mono<Double> getPresetDiversityScore( int presetId ){
        return this.databaseClient.sql("SELECT COUNT(DISTINCT categoryName) AS productCategoryNum FROM preset_detail\n" +
                "WHERE presetId = " + presetId + "\n").fetch().one()
                .flatMap(countObject -> {
                    int productCategoryNum = Integer.parseInt(String.valueOf(countObject.get("productCategoryNum")));
                    return this.databaseClient.sql("SELECT COUNT(*) AS presetNum FROM preset_detail\n" +
                            "WHERE presetId = " + presetId + "\n").fetch().one()
                            .flatMap(countObject2 -> {
                                double score = 0; // 모음집 점수 = 10 * productCategoryNum / presetNum
                                int presetNum = Integer.parseInt(String.valueOf(countObject2.get("presetNum")));
                                score = 10.0 * productCategoryNum / presetNum;
                                return Mono.just(score);
                            });
                }).switchIfEmpty(Mono.defer(() -> Mono.just(0.0D)));
    }

    // 개인화 평가 함수 1번 : 모음집 추천(좋아요) 여부 평가
    public Mono<Double> getDoRecommendScore( String userId , int presetId ){
        return this.databaseClient.sql("SELECT COUNT(*) AS doRecommend FROM user_preset_binding\n" +
                "WHERE userId =\"" + userId + "\"\n" +
                "AND presetId=" + presetId + "\n" +
                "AND recommend = 1").fetch().one()
                .flatMap(booleanObject -> {
                    boolean recommend = Double.parseDouble(String.valueOf(booleanObject.get("doRecommend"))) !=0; // 모음집 추천(좋아요) 여부
                    double score = 0; // 모음집 점수 = 추천(좋아요) True : 10, False : 0
                    if(recommend){
                        score = 10.0;
                    }
                    return Mono.just(score);
                }).switchIfEmpty(Mono.defer(() -> Mono.just(0.0D)));
    }

    // 개인화 평가 함수 2번 : 나와 비슷한 성향 유저가 구매한 모음집 여부 평가
    public Mono<Integer> getSimilarBuyPreferenceScore( String userId, int presetId ){
        return getSimilarBuyPreferencePresetList(userId)
                .collectList()
                .flatMap(integers -> {
                    if(integers.contains(presetId)) return Mono.just(10);
                    else return Mono.just(0);
                }).switchIfEmpty(Mono.defer(() -> Mono.just(0)));
    }

    public Flux<Integer> getSimilarBuyPreferencePresetList( String userId ){
        // 나와 비슷한 성향 유저가 구매한 모음집 목록 반환
        return this.databaseClient.sql("SELECT  upb2.presetId as presetId FROM user_preset_binding upb2\n" +
                "WHERE userId in\n" +
                "(\n" +
                "    SELECT DISTINCT upb1.userID FROM user_preset_binding upb1\n" +
                "    WHERE upb1.presetId in (\n" +
                "        SELECT DISTINCT presetId FROM user_preset_binding\n" +
                "        WHERE userId =\""+ userId +"\"\n" +
                "        AND buyCount > 0\n" +
                "        )\n" +
                "    AND upb1.userId !=\""+ userId +"\"\n" +
                ")\n" +
                "AND presetId not in\n" +
                "(\n" +
                "    SELECT DISTINCT presetId FROM user_preset_binding \n" +
                "        WHERE userId =\""+ userId +"\"\n" +
                "        AND buyCount > 0\n" +
                ")").fetch().all()//Flux
                .flatMap(stringObjectMap -> Flux.just(Integer.parseInt(String.valueOf(stringObjectMap.get("presetId")))))
                .switchIfEmpty(Mono.defer(() -> Mono.just(0)));
    }

    // 개인화 평가 함수 3번 : 나와 비슷한 성향 유저가 추천한 모음집 여부 평가
    public Mono<Double> getSimilarRecommendPreferenceScore( String userId, int presetId ){
        return getSimilarRecommendPreferencePresetList(userId).collectList()
                .flatMap(integers -> {
                    if(integers.contains(presetId)) return Mono.just(5.0D);
                    else return Mono.just(0.0D);
                }).switchIfEmpty(Mono.defer(() -> Mono.just(0.0D)));
    }

    public Flux<Integer> getSimilarRecommendPreferencePresetList(String userId ){
        return this.databaseClient.sql("SELECT  upb2.presetId as presetId FROM user_preset_binding upb2\n" +
                "WHERE userId in\n" +
                "(\n" +
                "    SELECT DISTINCT upb1.userID FROM user_preset_binding upb1\n" +
                "    WHERE upb1.presetId in (\n" +
                "        SELECT DISTINCT presetId FROM user_preset_binding\n" +
                "        WHERE userId =\""+ userId +"\"\n" +
                "        AND recommend > 0\n" +
                "        )\n" +
                "    AND upb1.userId !=\""+ userId +"\"\n" +
                ")\n" +
                "AND presetId not in\n" +
                "(\n" +
                "    SELECT DISTINCT presetId FROM user_preset_binding\n" +
                "        WHERE userId =\""+ userId +"\"\n" +
                "        AND recommend > 0\n" +
                ")").fetch().all()
                .flatMap(presetId -> Flux.just(Integer.parseInt(String.valueOf(presetId.get("presetId")))))
                .switchIfEmpty(Mono.defer(() -> Mono.just(0)));
    }

    // 개인화 평가 함수 4번 : 이전에 구매한 제품, 모음집 기반 평가
    public Mono<Double> getRelativeWithPurchasedHistoryScore( String userId, int presetId ){
        return Mono.zip(
                getPurchasedProductInPresetScore(userId,presetId),
                getPurcahsedPresetScore(userId, presetId),
                getPresetByPresetProducerScore(userId, presetId),
                getPresetByCategoryScore(userId,presetId)
        ).flatMap(objects -> Mono.just(objects.getT1() +
                objects.getT2() +
                objects.getT3() +
                objects.getT4()));
    }

    // 4.1번 : 구매 이력이 있는 제품의 개수 기반 평가
    public Mono<Double> getPurchasedProductInPresetScore( String userId, int presetId ){
        return this.databaseClient.sql("SELECT COUNT(*) AS purchasedProductInPresetNumber FROM purchase_history\n" +
                "WHERE productId in\n" +
                "(\n" +
                "SELECT productId FROM preset_detail\n" +
                "where presetId = "+ presetId + ")\n" +
                "AND userId =\"" + userId +"\"\n").fetch().one()
                .flatMap(stringObjectMap -> {
                    int purchasedProductInPresetNumber = Integer.parseInt(String.valueOf(stringObjectMap.get("purchasedProductInPresetNumber")));
                    return this.databaseClient.sql("SELECT COUNT(*) AS presetNumber FROM preset_detail\n" +
                            "where presetId = " + presetId).fetch().one()
                            .flatMap(countObject -> {
                                int presetNumber = Integer.parseInt(String.valueOf(countObject.get("presetNumber")));
                                double score = 0; // 모음집 점수 = 5 * purchasedProductInPresetNumber / presetNumber
                                score = 5.0 * purchasedProductInPresetNumber / presetNumber;
                                return Mono.just(score);
                            }).switchIfEmpty(Mono.defer(() -> Mono.just(0.0D)));
                }).switchIfEmpty(Mono.defer(() -> Mono.just(0.0D)));
    }

    // 4.2번 : 모음집 구매 이력 여부 기반 평가
    public Mono<Double> getPurcahsedPresetScore( String userId, int presetId ){
        return this.databaseClient.sql("SELECT COUNT(*) AS buyCount FROM user_preset_binding\n" +
                "WHERE presetId = " + presetId +"\n" +
                "AND userId = \"" + userId +"\"\n" +
                "AND buyCount > 0;\n").fetch().one()
                .flatMap(countObject -> {
                    double score = 0; //
                    int buyCount = Integer.parseInt(String.valueOf(countObject.get("buyCount")));
                    if(buyCount > 0){
                        score = 5;
                    }
                    return Mono.just(score);
                }).switchIfEmpty(Mono.defer(() -> Mono.just(0.0D)));
    }

    // 4.3 번 : 구매 이력이 있는 모음집 제작자의 다른 모음집 기반 평가
    public Mono<Double> getPresetByPresetProducerScore( String userId, int presetId ) {
        return getPresetByPresetProducer(userId, presetId)
                .collectList()
                .flatMap(integers -> {
                    if (integers.contains(presetId)) return Mono.just(5.0D);
                    else return Mono.just(0.0D);
                }).switchIfEmpty(Mono.defer(() -> Mono.just(0.0D)));
    }

    public Flux<Integer> getPresetByPresetProducer( String userId, int presetId){
        // 구매 이력이 있는 모음집 제작자의 다른 모음집 목록 반환
        return this.databaseClient.sql("SELECT presetId FROM preset\n" +
                "WHERE producer in\n" +
                "      (\n" +
                "          SELECT DISTINCT p.producer\n" +
                "          FROM preset p\n" +
                "                   JOIN user_preset_binding upb on p.presetId = upb.presetId\n" +
                "          WHERE upb.buyCount > 0\n" +
                "            AND upb.userId = \"" + userId + "\"\n" +
                "            AND p.producer != \"" + userId + "\"\n" +
                "      )\n" +
                "AND presetId != " + presetId + "\n").fetch().all()//Flux
                .flatMap(stringObjectMap -> {
                    int presetByPresetProducer = Integer.parseInt(String.valueOf(stringObjectMap.get("presetId")));
                    return Flux.just(presetByPresetProducer);
                }).switchIfEmpty(Mono.defer(() -> Mono.just(0)));
    }

    // 4.4 번 : 구매 이력이 있는 모음집 카테고리 기반 평가
    public Mono<Double> getPresetByCategoryScore( String userId, int presetId ){
        return getPresetByCategory(userId, presetId).collectList()
                .flatMap(integers -> {
                    if(integers.contains(presetId)) return Mono.just(5.0D);
                    else return Mono.just(0.0D);
                });
    }

    public Flux<Integer> getPresetByCategory( String userId, int presetId ){
        // 구매 이력이 있는 모음집 카테고리와 같은 카테고리를 가지는 모음집 목록 반환, 단 구매 이력이 있는 모음집은 반환하지 않는다.
        return this.databaseClient.sql("SELECT presetId FROM preset\n" +
                "WHERE categoryName in\n" +
                "(\n" +
                "    SELECT DISTINCT p.categoryName FROM preset p\n" +
                "    JOIN user_preset_binding upb on p.presetId = upb.presetId\n" +
                "    WHERE upb.userId = \"" + userId + "\"\n" +
                "    AND upb.buyCount > 0\n" +
                ")\n" +
                "AND presetId not in\n" +
                "(\n" +
                "    SELECT presetId FROM user_preset_binding\n" +
                "    WHERE userId = \"" + userId + "\"\n" +
                "    AND buyCount > 0\n" +
                "    )").fetch().all()//Flux
                .flatMap(presetIds -> {
                    int presetByCategory = Integer.parseInt(String.valueOf(presetIds.get("presetId")));
                    return Flux.just(presetByCategory);
                }).switchIfEmpty(Mono.defer(() -> Mono.just(0)));
    }

}
