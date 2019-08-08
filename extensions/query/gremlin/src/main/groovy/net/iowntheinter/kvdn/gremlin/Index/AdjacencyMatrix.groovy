package net.iowntheinter.kvdn.gremlin.Index
//package net.iowntheinter.kvdn.gremlin.Index
//
//import org.nd4j.linalg.api.ndarray.INDArray
//import org.nd4j.linalg.factory.Nd4j
//import org.nd4j.linalg.indexing.NDArrayIndex
//
//import java.util.concurrent.atomic.AtomicLong
//
//class AdjacencyMatrix {
//
//    /*
//    -------   stride = 1 (* tilesize)
//    + + + +
//    + + + +
//    + + + +
//    + + + +
//    if tile size is at first 4, we have one entry in the tile refrence.
//    t1
//    -------  -------  stride = 2 (* tile size)
//    + + + +  + + + +
//    + + + +  + + + + t2
//    + + + +  + + + +
//    + + + +  + + + +
//
//    + + + +  + + + +
//    + + + +  + + + +
//    + + + +  + + + +
//    + + + +  + + + +  if tile size grows, we have 4 entries in the ref arraylist
//     t3       t4
//    we added 3 new tiles
//    (t) 1 + (t) 2 = 3 + step 2 = 5
//
//    + + + +  + + + +  + + + + t5
//    + + + +  + + + +  + + + +
//    + + + +  + + + +  + + + +
//    + + + +  + + + +  + + + +
//
//    + + + +  + + + +  + + + +  t6
//    + + + +  + + + +  + + + +
//    + + + +  + + + +  + + + +
//    + + + +  + + + +  + + + +
//
//    + + + +  + + + +  + + + +
//    + + + +  + + + +  + + + +
//    + + + +  + + + +  + + + +
//    + + + +  + + + +  + + + +
//    t7        t8      t9
//    (t) 2 + (t) 5 = 7 + step 3 = 10
//    + + + +  + + + +  + + + +  + + + +  t10
//    + + + +  + + + +  + + + +  + + + +
//    + + + +  + + + +  + + + +  + + + +
//    + + + +  + + + +  + + + +  + + + +
//
//    + + + +  + + + +  + + + +  + + + +  t11
//    + + + +  + + + +  + + + +  + + + +
//    + + + +  + + + +  + + + +  + + + +
//    + + + +  + + + +  + + + +  + + + +
//
//    + + + +  + + + +  + + + +  + + + +  t12
//    + + + +  + + + +  + + + +  + + + +
//    + + + +  + + + +  + + + +  + + + +
//    + + + +  + + + +  + + + +  + + + +
//
//    + + + +  + + + +  + + + +  + + + +
//    + + + +  + + + +  + + + +  + + + +
//    + + + +  + + + +  + + + +  + + + +
//    + + + +  + + + +  + + + +  + + + +
//    t13      t14      t15      t16
//    t(5) + (t) 10 = 15 + step 4 = 19
//    + + + +  + + + +  + + + +  + + + +  + + + + t17
//    + + + +  + + + +  + + + +  + + + +  + + + +  ...
//    + + + +  + + + +  + + + +  + + + +  + + + +  t18 t19 t20
//    + + + +  + + + +  + + + +  + + + +  + + + +
//    ...
//
//    + + + +
//    + + + +
//    + + + +
//    + + + +
//    (t)21 .. 22 23 24 25
//
//     we added 6
//    a row stride is now 3
//    [ tile1, tile2
//
//     */
//    //^ is this worth it? can we just alloc a new array and copy the old one into it then drop the old array?
//
//    int cardinality
//    //int tileSize
//    //AtomicLong tileCount
//    INDArray indx = Nd4j.create(cardinality,cardinality); //output
//
//
//
//    int stride
//    private grow(){
//
//        cardinality = cardinality* 2
//        INDArray newIndx = Nd4j.create(cardinality,cardinality)
//        newIndx.assign(indx)
//
//    }
//
//    int edgeAt(int vertexID1, int vertexID2){
//        return indx.getInt(vertexID1,vertexID2)
//    }
//
//
//
//}
