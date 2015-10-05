/*
	UFSCar - BCC 2015-2 - Sistemas Distribuídos
	Trabalho 3 - Algoritmo do valentão

	Filipe Santos Rocchi 552194
	Rafael Brandão Barbosa Fairbanks 552372
*/

package valentao;

public class Valentao {

	public static void main(String[] args) throws InterruptedException {
		
		// cria e inicia threads
		for(int i = 0; i < Node.NUM_NODES; i++){
			if(i!=Node.NUM_NODES-1)
				Node.nodes.add(new Node(i, false));
			else
				Node.nodes.add(new Node(i, true)); // para o de maior id, coordenador ele será
		}
		
		System.out.println("\nMAIN: Starting Nodes.");
		for(Node n : Node.nodes){
			n.start();
		}
		
		Thread.sleep(500);
		
		Integer num = Node.NUM_NODES-1;
		// chama shutdown da thread coordenador
		System.out.println("Removendo coordenador "+num);
		Node coord = Node.nodes.get(num);
		coord.shutdownBully();
		
		for(Node n : Node.nodes){
			n.join();
		}
		
	}
	
}
