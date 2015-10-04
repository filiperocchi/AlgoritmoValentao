/*
 UFSCar - BCC 2015-2 - Sistemas Distribuídos
 Trabalho 3 - Algoritmo do valentão

 Filipe Santos Rocchi 552194
 Rafael Brandão Barbosa Fairbanks 552372
*/

package valentao;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Node extends Thread {
	// Attributes
	
	public static Integer NUM_NODES = 7;

	public static ArrayList<Node> nodes = new ArrayList<>();

	public Integer id;
	public Boolean coordenador;
	public Boolean alive;
	public Boolean recebiOk;

	private final Queue<Integer> msgsToDo;

	private Boolean debug = true;
	private Integer printId;
	
	// Methods
	Node(Integer i, Boolean c) {

		id = i;
		coordenador = c;
		alive = true;
		recebiOk = false;

		msgsToDo = new ConcurrentLinkedQueue<Integer>();

		printId=5;
		if (debug && printId==id) {
			System.out.println("Node " + id + " criado, coord:"+coordenador+", alive:"+alive);
		}
	}

	@Override
	public void run() {
		if(debug && printId==id) System.out.println("Node "+id+": run");
		
		Thread t1 = new Thread(() -> {
			while (alive) {
				Integer i = msgsToDo.poll();
				
				while (i != null) {
					processMsg(i);
					i = msgsToDo.poll();
				}
			}
		});
		t1.start();
		
		//if(debug && printId==id) System.out.println("Node "+id+": posThread processMsg");

		while (alive) {
			while (true) {
				if (!coordenador && !isBullyAlive()) { // se o bully estiver vivo, faz nada
					if(printId==id) System.out.println("Node " + id + " detectou ausência de coordenador");
					break; // se não, sai e começa o processo de eleição
				}
			}

			if (becomeBully()) { // caso eu tenha me tornado o coordenador
				
				if(printId==id) System.out.println("node"+id+": Sou o novo coordenador");
				//*/
				try {
					Thread.sleep(5000); // dorme uns 5sec
				} catch (InterruptedException ex) {
					Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
				}//*/
				shutdownBully();
			} else { // caso alguem esteja começando uma nova eleição
				if(debug && printId==id) System.out.println("node"+id+": alguem começou outra eleição");
				//*
				try {
					Thread.sleep(500); // dorme uns .5sec
				} catch (InterruptedException ex) {
					Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
				}//*/

				recebiOk = false;
			}
		}

	}

	public Boolean isBullyAlive() {
		if (debug && printId==id) {
			//System.out.println("node" + id + ": isBullyAlive");
		}

		for (Node n : nodes) { // roda pelos nós
			if (n.id > this.id) { // para os maiores que eu
				if (n.coordenador) { // verifica se é coordenador
					if (n.alive) { // retorna se está vivo ou não
						return true;
					}

					return false;
				}
			}
		}

		return false; // caso não haja coordenador
	}

	public Boolean becomeBully() {
		if (debug && printId==id) {
			System.out.println("node" + id + ": becomeBully");
		}

		sendMsg(); // manda as mensagens pra galera

		int i = 0;
		while (i < 50000) {
			if (recebiOk == false) {
				i++; // segue o loop
			} else {
				return false; // caso tenha recebido algum Ok, retorna false
			}
		}

		coordenador = true; // caso não receba Oks após algum tempo, é o eleito
		return true;
	}

	public void shutdownBully() {
		if (debug && printId==id) {
			System.out.println("node" + id + ": shutdownBully");
		}

		alive = false;
	}

	public void receiveOk(Integer idSender) {
		if (debug && printId==id) {
			System.out.println("node" + id + ": receiveOk; Sender: "+idSender);
		}

		recebiOk = true;
	}

	public void sendMsg() {
		if (debug && printId==id) {
			System.out.println("node" + id + ": sendMsg");
		}

		for (int i = this.id; i < nodes.size(); i++) { // para os nós maiores que eu
			Node n = nodes.get(i);

			if (n.id > this.id) {
				n.receiveMsg(this.id); // faz eles receberem a mensagem com meu id
			}
		}
	}

	public void receiveMsg(Integer idSender) {
		if (debug && printId==id) {
			System.out.println("node" + id + ": receiveMsg; Sender: "+idSender);
		}

		msgsToDo.add(idSender);
	}

	public void processMsg(Integer idSender) {
		if (idSender == null) {
			throw new RuntimeException();
		}
		
		if (debug && printId==id) {
			System.out.println("node" + id + ": processMsg, sending OK to "+idSender);
		}

		Node n = nodes.get(idSender);
		n.receiveOk(this.id);
	}

}
