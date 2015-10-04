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
	
	public Integer clock;

	private final Queue<Integer> msgsToDo;

	private Boolean debug = true;
	private Integer printId;
	
	// Methods
	Node(Integer i, Boolean c) {

		id = i;
		coordenador = c;
		alive = true;
		recebiOk = false;
		
		clock = 0;

		msgsToDo = new ConcurrentLinkedQueue<Integer>();

		printId=id; // PARA IMPRESSAO DE APENAS 1 NÓ COLOQUE O NÚMERO DELE, i PARA TODOS
		
		if (debug && printId==id) System.out.println("Node " + id+" (clock:"+clock+ ") criado, coord:"+coordenador+", alive:"+alive);
	}

	@Override
	public void run() {
		if(debug && printId==id) System.out.println("Node "+id+" (clock:"+clock+"): run");
		
		Thread t1 = new Thread(() -> { // Thread para tratar a troca de mensagens em paralelo (usa lambda function ()->)
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
				if (!coordenador && !isBullyAlive()) { // se eu não for o coord e o bully estiver vivo, faz nada
					if(printId==id) System.out.println("Node " + id+" (clock:"+clock+") detectou ausência de coordenador");
					break; // se não, sai e começa o processo de eleição
				}
			}
			
			clock++;

			if (becomeBully()) { // caso eu tenha me tornado o coordenador
				if(printId==id) System.out.println("node"+id+" (clock:"+clock+"): Sou o novo coordenador");
				
				//*/
				try {
					Thread.sleep(1000); // dorme uns 1sec
				} catch (InterruptedException ex) {
					Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
				}//*/
				shutdownBully();
			} else { // caso alguem esteja começando uma nova eleição
				if(debug && printId==id) System.out.println("node"+id+" (clock:"+clock+"): alguem começou outra eleição");
				//*
				try {
					Thread.sleep(200); // dorme uns .2sec
				} catch (InterruptedException ex) {
					Logger.getLogger(Node.class.getName()).log(Level.SEVERE, null, ex);
				}//*/

				recebiOk = false;
			}
			
		}

	}

	public Boolean isBullyAlive() {
		if (debug && printId==id); //System.out.println("node" + id+" (clock:"+clock+"): isBullyAlive");
		
		for (int i = this.id+1; i < nodes.size(); i++) { // para os nós maiores que eu
			Node n = nodes.get(i);
			if (n.coordenador) { // verifica se é coordenador
				if (n.alive) { // retorna se está vivo ou não
					return true;
				}
				return false;
			}
		}

		return false; // caso não haja coordenador
	}

	public Boolean becomeBully() {
		if (debug && printId==id) System.out.println("node" + id+" (clock:"+clock+"): becomeBully");

		sendMsg(); // manda as mensagens pra galera

		Integer i = 0;
		while (i < 500000) {
			if (recebiOk == false) {
				i++; // segue o loop
			} else {
				msgsToDo.clear();
				return false; // caso tenha recebido algum Ok, retorna false
			}
		}

		coordenador = true; // caso não receba Oks após algum tempo, é o eleito
		return true;
	}

	public void shutdownBully() {
		if (debug && printId==id) System.out.println("node" + id+" (clock:"+clock+"): shutdownBully");

		alive = false;
	}

	public void receiveOk(Integer idSender) {
		if (debug && printId==id) System.out.println("node" + id+" (clock:"+clock+"): receiveOk; Sender: "+idSender);

		recebiOk = true;
	}

	public void sendMsg() {
		if (debug && printId==id) System.out.println("node" + id+" (clock:"+clock+"): sendMsg");

		for (int i = this.id+1; i < nodes.size(); i++) { // para os nós maiores que eu
			Node n = nodes.get(i);
			
			if (1==0 && printId==id) System.out.println("node"+id+" (clock:"+clock+"): sendMsg to "+n.id+", isAlive: "+n.alive);
			
			if (n.alive) { // e que estejam vivos/ativos
				n.receiveMsg(this.id); // faz eles receberem a mensagem com meu id
			}
		}
	}

	public void receiveMsg(Integer idSender) {
		if (debug && printId==id) System.out.println("node" + id+" (clock:"+clock+"): receiveMsg; Sender: "+idSender);

		msgsToDo.add(idSender);
	}

	public void processMsg(Integer idSender) {
		if (idSender == null) {
			throw new RuntimeException();
		}
		
		if (debug && printId==id) System.out.println("node" + id+" (clock:"+clock+"): processMsg, sending OK to "+idSender);

		Node n = nodes.get(idSender);
		n.receiveOk(this.id);
	}

}
