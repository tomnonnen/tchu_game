package ch.epfl.tchu.game;

import ch.epfl.tchu.Preconditions;

/**
 * @author Tom Nonnenmacher (Sciper : 325341)
 * @author Théo Ducrey (Sciper : 324915)
 */
public final class StationPartition implements StationConnectivity{

    private final int[] stationTabId; //0->50 nbr de Station dans le tChu

    /**
     * private constructor that initializes stationEnsembleID
     * @param stationEnsembleID: array of integers containing the links linking each element to its subset representative
     */
    private StationPartition(int[] stationEnsembleID) {
        this.stationTabId = stationEnsembleID;
    }


    /**
     * @param s1 departure station
     * @param s2 station of arrival
     * @return When at least one of the stations passed to it is thus out of bounds, it returns true only if both
     * stations have the same identity. Otherwise it returns true if they belong to the same sets
     */
    @Override
    public boolean connected(Station s1, Station s2) {

        if((s1.id() >= stationTabId.length) || (s2.id() >= stationTabId.length)){ //if a station are out of the array
            return s1.id() == s2.id();
        } else { // if they are in the table
            return stationTabId[s1.id()]==stationTabId[s2.id()];
        }

    }



    public static final class Builder{

        public final int[] buildStationTabID;

        /**
         * constructs a partition builder of a set of stations whose identity is between 0 (included) and stationCount (excluded)
         * @param stationCount nbr of stations in the part (of the map)
         * @throws IllegalArgumentException if stationCount is strictly negative (< 0)
         */
        public Builder(int stationCount){
            Preconditions.checkArgument(stationCount>=0);
            buildStationTabID = new int[stationCount];

            //initial attribution for all station itself as group representative
            for (int i=0;i<buildStationTabID.length;i++) {
                buildStationTabID[i]=i;
            }
        }


        /**
         * @param idStation the identification number of a station
         * @return the identification number of the representative of the subset containing it.
         */
        private int representative(int idStation){
            int representative = idStation;
            boolean found = false;
            while (!found){
                found = representative ==buildStationTabID[representative];
                representative = buildStationTabID[representative];
            }
            return representative;
        }

        /**
         * joins the subsets containing the two stations passed as arguments, taking the represent of the station1 as representative of the joined subset
         * @param s1 station to be specified as connected
         * @param s2 station to be specified as connected
         * @return the builder (this)
         */
        public Builder connect(Station s1, Station s2){
            //we connect the representative of the group of station1 one to the representative of the group of station2
            // => by doing this we connect indirectly all the elem of the group of station1 to the group of station2
            buildStationTabID[representative(s1.id())] = buildStationTabID[representative(s2.id())];
            return this;
        }

        /**
         * @return the flattened partition of stations corresponding to the deep partition being built by this builder
         */
        public StationPartition build() {

            //We attribute to all station is corresponding group station ( 0->1->2 => 0->2)
            for (int i =0;i<buildStationTabID.length;i++) {
                buildStationTabID[i] = representative(i);
            }

            return new StationPartition(buildStationTabID);
        }


    }
}
