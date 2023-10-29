import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class LeitoresEscritores {
    private int leitoresAtivos = 0;
    private boolean escritaAtiva = false;
    private Random rand = new Random();
    private ReentrantLock lock = new ReentrantLock();
    private Condition leituraCond = lock.newCondition();
    private Condition escritaCond = lock.newCondition();

    public void iniciarLeitura() {
        try {
            System.out.println("Thread " + Thread.currentThread().threadId() + " tentando iniciar leitura");

            lock.lock();
            while (escritaAtiva) {
                leituraCond.await();
            }
            leitoresAtivos++;

            System.out.println("Leitura iniciada por Thread " + Thread.currentThread().threadId());

            lock.unlock();

            Thread.sleep(rand.nextInt(2000));

            lock.lock();
            leitoresAtivos--;

            if (leitoresAtivos == 0) {
                escritaCond.signal();
            }

            System.out.println("Leitura concluída por Thread " + Thread.currentThread().threadId());
            lock.unlock();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void iniciarEscrita() {
        try {
            System.out.println("Thread " + Thread.currentThread().threadId() + " tentando iniciar escrita");

            lock.lock();
            while (leitoresAtivos > 0 || escritaAtiva) {
                escritaCond.await();
            }
            escritaAtiva = true;

            System.out.println("Escrita iniciada por Thread " + Thread.currentThread().threadId());

            lock.unlock();

            Thread.sleep(rand.nextInt(2000));

            lock.lock();
            escritaAtiva = false;
            System.out.println("Escrita concluída por Thread " + Thread.currentThread().threadId());
            leituraCond.signalAll();
            escritaCond.signal();
            lock.unlock();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        LeitoresEscritores leitoresEscritores = new LeitoresEscritores();
        Random rand = new Random();

        for (int i = 0; i < 20; i++) {
            Thread leitor = new Thread(() -> {
                leitoresEscritores.iniciarLeitura();
            });
            Thread.sleep(rand.nextInt(2000));
            leitor.start();
            if (rand.nextBoolean()) {
                Thread escritor = new Thread(() -> {
                    leitoresEscritores.iniciarEscrita();
                });
                Thread.sleep(rand.nextInt(2000));
                escritor.start();
            }
        }
    }
}
