package org.apache.commons.graph.decorator;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.graph.DirectedGraph;
import org.apache.commons.graph.Edge;
import org.apache.commons.graph.GraphException;
import org.apache.commons.graph.Vertex;
import org.apache.commons.graph.WeightedEdge;
import org.apache.commons.graph.WeightedGraph;
import org.apache.commons.graph.WeightedPath;
import org.apache.commons.graph.algorithm.path.AllPairsShortestPath;
import org.apache.commons.graph.algorithm.spanning.MinimumSpanningForest;
import org.apache.commons.graph.domain.basic.DirectedGraphImpl;
import org.apache.commons.graph.domain.basic.DirectedGraphWrapper;

/**
 * Description of the Class
 */
public class DecoratedDirectedGraph<V extends Vertex, WE extends WeightedEdge<V>>
    extends DirectedGraphWrapper<V, WE>
    implements DirectedGraph<V, WE>, WeightedGraph<V, WE>
{

    private static final Map<DirectedGraph<? extends Vertex, ? extends Edge>, DecoratedDirectedGraph<? extends Vertex, ? extends Edge>>
    DECORATED_GRAPHS = new HashMap<DirectedGraph<? extends Vertex, ? extends Edge>, DecoratedDirectedGraph<? extends Vertex, ? extends Edge>>();// DGRAPH X DDGRAPH

    /**
     * Description of the Method
     */
    public static <V extends Vertex, WE extends WeightedEdge<V>> DecoratedDirectedGraph<V, WE> decorateGraph( DirectedGraph<V, WE> graph )
    {
        if ( graph instanceof DecoratedDirectedGraph )
        {
            return (DecoratedDirectedGraph<V, WE>) graph;
        }

        if ( DECORATED_GRAPHS.containsKey( graph ) )
        {
            @SuppressWarnings( "unchecked" ) // driven by graph parameter type
            DecoratedDirectedGraph<V, WE> decorated = (DecoratedDirectedGraph<V, WE>) DECORATED_GRAPHS.get( graph );
            return decorated;
        }

        DecoratedDirectedGraph<V, WE> decorated = new DecoratedDirectedGraph<V, WE>( graph );
        DECORATED_GRAPHS.put( graph, decorated );
        return decorated;
    }

    private final WeightedGraph<V, WE> weighted;

    private Map<WE, Number> weights = new HashMap<WE, Number>();// EDGE X DOUBLE

    private AllPairsShortestPath allPaths = null;

    /**
     * Constructor for the DDirectedGraph object
     *
     * @param impl
     */
    protected DecoratedDirectedGraph( DirectedGraph<V, WE> impl )
    {
        super( impl );

        if ( impl instanceof WeightedGraph )
        {
            @SuppressWarnings( "unchecked" ) // impl is DirectedGraph<V, WE>
            WeightedGraph<V, WE> tmp = (WeightedGraph<V, WE>) impl;
            weighted = tmp;
        }
        else
        {
            weighted = null;
        }
    }

    // WeightedGraph Implementation

    /**
     * Description of the Method
     */
    public DirectedGraph<V, WE> transpose()
        throws GraphException
    {
        try
        {
            DirectedGraphImpl<V, WE> directedGraph = new DirectedGraphImpl<V, WE>();
            Set<V> vertexSet = getVertices();
            Set<WE> edgeSet = getEdges();

            Iterator<V> vertices = vertexSet.iterator();
            while ( vertices.hasNext() )
            {
                directedGraph.addVertex( vertices.next() );
            }

            Iterator<WE> edges = edgeSet.iterator();
            while ( edges.hasNext() )
            {
                WE edge = edges.next();

                directedGraph.addEdge( edge, edge.getTail(), edge.getHead() );
            }

            return directedGraph;
        }
        catch ( GraphException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            throw new GraphException( e );
        }
    }

    /**
     * Description of the Method
     */
    public boolean hasConnection( Vertex start, Vertex end )
        throws GraphException
    {
        if ( start == end )
        {
            return true;
        }

        try
        {
            if ( allPaths == null )
            {
                allPaths = new AllPairsShortestPath( this );
            }
            else
            {
                allPaths.update( this );
            }

            WeightedPath<V, WE> path = allPaths.getShortestPath( start, end );
        }
        catch ( GraphException ex )
        {
            return false;
        }

        return true;
    }

    public MinimumSpanningForest minimumSpanningForest()
    {
        return new MinimumSpanningForest( this );
    }

    public MinimumSpanningForest maximumSpanningForest()
    {
        return new MinimumSpanningForest( false, this );
    }

}
