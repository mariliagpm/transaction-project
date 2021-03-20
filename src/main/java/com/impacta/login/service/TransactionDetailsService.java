package com.impacta.login.service;

import com.impacta.login.model.TransactionDao;
import com.impacta.login.model.TransactionDTO;

import com.impacta.login.repository.TransactionRepository;

import java.sql.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionDetailsService {

	@Autowired
	private TransactionRepository movimentacaoRepository;

	public TransactionDetailsService(TransactionRepository movimentacaoRepository) {
		super();
		this.movimentacaoRepository = movimentacaoRepository;
	}

	public List<TransactionDao> searchByAccountId(int contaCorrenteId, int status) {
		System.out.println(contaCorrenteId);
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("movimentacoes");

		EntityManager manager = factory.createEntityManager();

		Query query = manager.createQuery("from movimentacoes as mo "
				+ "where mo.accountId = :accountId and mo.statusTransaction = :statusTransaction ");
		query.setParameter("accountId", contaCorrenteId);
		query.setParameter("statusTransaction", status);

		List<TransactionDao> lista = query.getResultList();
		return lista;
	}

	public double getBalance(int contaCorrenteId, int status) {
		EntityManagerFactory factory = Persistence.createEntityManagerFactory("movimentacoes");

		EntityManager manager = factory.createEntityManager();

		Query query = manager.createQuery("from movimentacoes as mo "
				+ "where mo.accountId = :accountId and mo.statusTransaction = :statusTransaction ");
		query.setParameter("accountId", contaCorrenteId);
		query.setParameter("statusTransaction", status);

		List<TransactionDao> listTransactions = query.getResultList();
		double saldo = 0.0;
		for (TransactionDao movimentacoesDao : listTransactions) {
			saldo = saldo + movimentacoesDao.getValue();
		}
		System.out.print("\n saldo "+saldo+"\n \n");
		return saldo;
		
	}

	public TransactionDao save(TransactionDTO transactionDTO) throws Exception {

		TransactionDao newTransaction = new TransactionDao();

		newTransaction.setAccountId(transactionDTO.getAccountId());
		newTransaction.setValue(transactionDTO.getValue());
		newTransaction.setDescription_transaction(transactionDTO.getDescription_transaction());
		newTransaction.setDescriptionStatus(transactionDTO.getDescriptionStatus());
		newTransaction.setStatusTransaction(transactionDTO.getStatusTransaction());
		newTransaction.setType_operation(transactionDTO.getType_operation());
		newTransaction.setMoney_name(transactionDTO.getMoney_name());
		newTransaction.setTypeTransactionId(transactionDTO.getTypeTransactionId());
		newTransaction.setMoney_simbol(transactionDTO.getMoney_simbol());
		newTransaction.setDescription_type_transaction(transactionDTO.getDescription_type_transaction());
		newTransaction.setCreatedDate(new Date(System.currentTimeMillis()));

		return movimentacaoRepository.save(newTransaction);

	}

}