package tallestegg.guardvillagers.entities;

public interface GuardVillagersVillagerData {

    int getTimesThrownPotion();

    int getTimesHealedGolem();

    int getTimesRepairedGuard();

    long getLastRepairedGolem();

    long getLastRepairedGuard();

    long getLastThrownPotion();

    void setTimesThrownPotion(int timesThrownPotion);

    void setTimesHealedGolem(int timesHealedGolem);

    void setTimesRepairedGuard(int timesRepairedGuard);

    void setLastRepairedGolem(long lastRepairedGolem);

    void setLastRepairedGuard(long lastRepairedGuard);

    void setLastHealedGuard(long lastHealedGuard);
}
