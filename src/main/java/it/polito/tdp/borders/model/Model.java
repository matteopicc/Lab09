package it.polito.tdp.borders.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import it.polito.tdp.borders.db.BordersDAO;

public class Model {
	private BordersDAO bordersDAO;
	private List<Country>countries;
	private CountryIdMap countryIdMap;
	private SimpleGraph<Country,DefaultEdge>graph;

	public Model() {
		bordersDAO = new BordersDAO();
	}
	
	public void createGraph(int anno) {
		countryIdMap = new CountryIdMap();
		countries = bordersDAO.loadAllCountries(countryIdMap);
		List<Border>confini = bordersDAO.getCountryPairs(countryIdMap, anno);
		if(confini.isEmpty()) {
			throw new RuntimeException("Non sono presenti confini per l'anno selezionato");
		}
		graph = new SimpleGraph<>(DefaultEdge.class);
		for(Border b : confini) {
			graph.addVertex(b.getC1());//aggiunta dei vertici al grafo
			graph.addVertex(b.getC2());
			graph.addEdge(b.getC1(), b.getC2()); //aggiunta degli archi al grafo
		}
		countries = new ArrayList<>(graph.vertexSet());//salvataggio del set di vertici in una lista
		Collections.sort(countries);//ordinamento alfabetico della lista di vertici
	}

	public List<Country>getCountries(){
		if(countries == null) {
			return new ArrayList<Country>();
		}
		return countries;
	}
	
	//mappa contenente il grado del vertice
	public Map<Country,Integer>getCountryCounts(){
		if(graph == null) {
			throw new RuntimeException("Grafo non esistente");
		}
		Map<Country,Integer> stats = new HashMap<Country,Integer>();
		for(Country c : countries) {
			stats.put(c, graph.degreeOf(c));
		}
		return stats;
	}
	
	//numero componenti connesse del grafo
	public int getNumberOfConnectedComponents() {
		if(graph == null) {
			throw new RuntimeException("Grafo non esistente");
		}
		ConnectivityInspector<Country,DefaultEdge>ci = new ConnectivityInspector<Country,DefaultEdge>(graph);
		return ci.connectedSets().size();
		
	}
	
	public List<Country>getReachableCountries(Country selectedCountry){
		if(!graph.vertexSet().contains(selectedCountry)) {
			throw new RuntimeException("Paese selezionato non esistente");
		}
		List<Country>reachableCountries = this.displayAllNeighboursIterative(selectedCountry);
		//System.out.println("Reachable Countries: "+reachableCountries.size());
		//List<Country>reachableCountries = this.displayAllNeighboursJGrapht(selectedCountry);
		//System.out.println("Reachable Countries: "+reachableCountries.size());
		//List<Country>reachableCountries = this.displayAllNeighboursRecursive(selectedCountry);
		System.out.println("Reachable Countries: "+reachableCountries.size());
		return reachableCountries;
		
	}
	
	//versione iterativa

	private List<Country> displayAllNeighboursIterative(Country selectedCountry) {
		//CREA 2 LISTE: QUELLA DEI NODI VISITATI
		List<Country>visited = new LinkedList<Country>();
		//e quella dei nodi da visitare
		List<Country>toBeVisited = new LinkedList<Country>();
		//aggiungo alla lista dei vertici visitati il nodo di partenza
		visited.add(selectedCountry);
		//Aggiungo alla lista dei vertici da visitare tutti i vertici collegati a quello esistente
		toBeVisited.addAll(Graphs.neighborListOf(graph, selectedCountry));
		
		while(toBeVisited.isEmpty()) {
			//Rimuovi il vertice in testa alla coda
			Country temp = toBeVisited.remove(0);
			//aggiungi il nodo alla lista dei visitati
			visited.add(temp);
			//ottieni tutti i vicini di un nodo
			List<Country>listaDeiVicini = Graphs.neighborListOf(graph, temp);
			//rimuovi da quella lista quelli che hai gia visitato
			listaDeiVicini.removeAll(visited);
			//e quelli che sai gia che devi visitare
			listaDeiVicini.removeAll(toBeVisited);
			//aggiungi i rimanenti alla coda di quelli da visitare
			toBeVisited.addAll(listaDeiVicini);
		}
		//ritorna la lista di tutti i nodi raggiungibili
		return visited;
	}
	
	//versione libreria JGRAPHT
	
	private List<Country> displayAllNeighboursJGrapht(Country selectedCountry){
		List<Country>visited = new LinkedList<Country>();
		
		//Versione 1: Utilizzo un BreadthFirstIterator
		//GraphIterator<Country, DefaultEdge>bfv = new BreadthFirstIterator<Country, DefaultEdge>(graph,selectedCountry);
		//while(bfv.hasNext()){
		//visited.add(bfv.next());
		//}
		
		//Versione 2: Utilizzo un DepthFirstIterator
		GraphIterator<Country, DefaultEdge>dfv = new DepthFirstIterator<Country, DefaultEdge>(graph,selectedCountry);
		while(dfv.hasNext()){
				visited.add(dfv.next());
				}
		return visited;
	
	}
	
	//versione ricorsiva
	private List<Country> displayAllNeighboursRecursive(Country selectedCountry){
		List<Country>visited = new LinkedList<Country>();
		recursiveVisit(selectedCountry,visited);
		return visited;
	}

	private void recursiveVisit(Country n, List<Country> visited) {
		//sempre
		visited.add(n);
		
		//ciclo
		for(Country c: Graphs.neighborListOf(graph, n)) {
			if(!visited.contains(c)) {
				recursiveVisit(c,visited);
				//non rimuoverlo per fare backtracking
			}
		}	
	}
}
