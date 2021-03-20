package com.impacta.login.controller;

import com.impacta.login.model.Extrato;
import com.impacta.login.model.TransactionDao;
import com.impacta.login.model.TransactionDTO;
import com.impacta.login.model.Balance;
import com.impacta.login.service.TransactionDetailsService;

 
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class TransactionController extends AbstractController {

	@Autowired
	private TransactionDetailsService movimentacaoDetailsService;

	private static final Logger LOGGER = Logger.getLogger(TransactionController.class);

	private static final String FEEDBACK_MESSAGE_TRANSACTION_CREATED_SUCCESS = "feedback.message.transaction.created.success";
	private static final String ERROR_MESSAGE_TRANSACTION_NOT_CREATED = "error.message.transaction.not.created";
	private static final String FEEDBACK_MESSAGE_BALANCE_AVAILABLE = "feedback.message.balance.available";
	private static final String ERROR_MESSAGE_BALANCE_NOT_AVAILABLE = "error.message.balance.not.available";
	private static final String FEEDBACK_MESSAGE_ALL_TRANSACTIONS_AVAILABLE = "feedback.message.all.transactions.available";
	private static final String ERROR_MESSAGE_ALL_TRANSACTIONS_NOT_AVAILABLE = "error.message.all.transactions.not.available";

	private static final String ERROR_MESSAGE_DEPOSIT_VALUE_NOT_VALID = "error.message.deposit.value.not.valid";
	private static final String ERROR_MESSAGE_DEPOSIT_NOT_AVAILABLE = "error.message.deposit.not.available";
	private static final String FEEDBACK_MESSAGE_DEPOSIT_CREATED = "feedback.message.deposit.was.created";

	private static final String ERROR_MESSAGE_WITHDRAWAL_UNAVAILABLE = "error.message.withdrawal.not.available";
	private static final String ERROR_MESSAGE_WITHDRAWAL_VALUE_NOT_VALID = "error.message.withdrawal.value.not.valid";
	private static final String FEEDBACK_MESSAGE_WITHDRAWAL_CREATED = "feedback.message.withdrawal.was.created";
	private static final String ERROR_MESSAGE_TRANSFER_VALUE_NOT_VALID = "error.message.transfer.value.not.valid";

	private static final String ERROR_MESSAGE_NO_MONEY_TO_THIS_TRANSACTION = "error.message.no.money.to.this.transaction";
	private static final String ERROR_MESSAGE_WRONG_BALANCE_AFTER_TRANSACTION = "error.message.wrong.balance.after.transaction";
	private static final String ERROR_MESSAGE_TRANSFER_UNAVAILABLE = "error.message.transfer.not.available";

	private static final String FEEDBACK_MESSAGE_TRANSFER_CREATED_SUCCESS = "feedback.message.transfer.was.created";

	@RequestMapping(value = "api/transaction/addTransaction", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> saveMovimetacao(@RequestBody TransactionDTO transacation) throws Exception {
		try {
			movimentacaoDetailsService.save(transacation);
			return new ResponseEntity<>(addFeedbackMessage(FEEDBACK_MESSAGE_TRANSACTION_CREATED_SUCCESS),
					HttpStatus.CREATED);
		} catch (Exception e) {
			return new ResponseEntity<>(addFeedbackMessage(ERROR_MESSAGE_TRANSACTION_NOT_CREATED), HttpStatus.CREATED);

		}
	}
	
	@PreAuthorize("#oauth2.hasScope('read')")
	@RequestMapping(value = "api/transaction/balance", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getSaldo(@RequestBody TransactionDTO transacation) throws Exception {
		try {
			 
			int contaCorrenteIdUser = 1;
			int contaPoupancaIdUser = 2;
			LOGGER.info("Starting balance api");

			LOGGER.info("Get balance api for account 1");
			double saldoAtualContaCorrente = movimentacaoDetailsService.getBalance(contaCorrenteIdUser, 1);
			;

			LOGGER.info("Balance for account 1 " + saldoAtualContaCorrente);

			Balance saldoContaCorrente = new Balance(saldoAtualContaCorrente, "Conta corrente", "BRL", "R$");
			LOGGER.info("Get balance api account 2");

			double saldoAtualContaPoupanca = movimentacaoDetailsService.getBalance(2, 1);

			LOGGER.info("Balance account 2" + saldoAtualContaCorrente);

			Balance saldoContaPoupanca = new Balance(saldoAtualContaPoupanca, "Conta Poupança", "BRL", "R$");

			List<Balance> listaSaldo = new ArrayList<Balance>();
			listaSaldo.add(saldoContaCorrente);
			listaSaldo.add(saldoContaPoupanca);
			LOGGER.info("Listas saldos   " + listaSaldo.size());

			return new ResponseEntity<>(addFeedbackMessage(FEEDBACK_MESSAGE_BALANCE_AVAILABLE, listaSaldo),
					HttpStatus.OK);
		} catch (Exception e) {
			System.out.println("error " + e);
			return new ResponseEntity<>(addErrorMessage(ERROR_MESSAGE_BALANCE_NOT_AVAILABLE), HttpStatus.CREATED);
		}
	}

	@RequestMapping(value = "api/transaction/deposit", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> createDeposit(@RequestBody TransactionDTO transacation) throws Exception {
		try {
			if (transacation.getValue() <= 0) {
				return new ResponseEntity<>(addErrorMessage(ERROR_MESSAGE_DEPOSIT_VALUE_NOT_VALID),
						HttpStatus.INTERNAL_SERVER_ERROR);

			}

			transacation.setDescriptionStatus("Approved");
			transacation.setStatusTransaction(1);
			transacation.setType_operation("Credit");
			transacation.setMoney_name("BRL");
			transacation.setTypeTransactionId(2);
			transacation.setDescription_type_transaction("Enter - Deposit");
			transacation.setMoney_simbol("R$");
			movimentacaoDetailsService.save(transacation);
			return new ResponseEntity<>(addFeedbackMessage(FEEDBACK_MESSAGE_DEPOSIT_CREATED), HttpStatus.CREATED);

		} catch (Exception e) {

			return new ResponseEntity<>(addErrorMessage(ERROR_MESSAGE_DEPOSIT_NOT_AVAILABLE),
					HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}

	@RequestMapping(value = "api/transaction/transferByCPF", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> createdInternTransfer(@RequestBody TransactionDTO transacation) throws Exception {
		try {
			double value = transacation.getValue();
			if (transacation.getValue() <= 0) {
				return new ResponseEntity<>(addErrorMessage(ERROR_MESSAGE_TRANSFER_VALUE_NOT_VALID),
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
			int accountFrom = 1;
			int accountTo = 2;
			Double balanceOldFrom = movimentacaoDetailsService.getBalance(1, 1);
			Double balanceOldTO = movimentacaoDetailsService.getBalance(2, 1);

			if ((balanceOldFrom - value) < 0)
				return new ResponseEntity<>(addErrorMessage(ERROR_MESSAGE_NO_MONEY_TO_THIS_TRANSACTION),
						HttpStatus.INTERNAL_SERVER_ERROR);
			System.out.print("Log antes de fazer");

			System.out.print("Log antes de fazer retirada FROM " + balanceOldFrom);

			if ((balanceOldFrom - value) >= 0) {
				transacation.setAccountId(1);
				transacation.setDescription_transaction("Trasfer to 1 to 2");
				createWithDrawal(transacation);
				TransactionDTO deposit = new TransactionDTO();
				deposit.setAccountId(2);

				deposit.setValue(value);
				deposit.setDescription_transaction("Trasfer from 1 to 2");
				System.out.println(" " + deposit.toString());
				createDeposit(deposit);
			}

			Double balanceNewFrom = movimentacaoDetailsService.getBalance(1, 1);
			Double balanceNewTo = movimentacaoDetailsService.getBalance(2, 1);

			System.out.println("Balanco oldTO "+balanceOldTO);
			System.out.println("Balanco newTO "+balanceNewTo);
			System.out.println("Balanco oldFrom "+balanceOldFrom);
			System.out.println("Balanco newFrom "+balanceNewFrom);
			System.out.println("valor "+value);
			
			if ((balanceOldFrom - value != balanceNewFrom)) {
				System.out.println("saldo errado from "+value);
				
				return new ResponseEntity<>(addErrorMessage(ERROR_MESSAGE_WRONG_BALANCE_AFTER_TRANSACTION),
						HttpStatus.INTERNAL_SERVER_ERROR);

			}

			if ((balanceOldTO + value != balanceNewTo)) {
				System.out.println("saldo errado to "+value);
				 	return new ResponseEntity<>(addErrorMessage(ERROR_MESSAGE_DEPOSIT_VALUE_NOT_VALID),
						HttpStatus.INTERNAL_SERVER_ERROR);
			}
			return new ResponseEntity<>(addFeedbackMessage(FEEDBACK_MESSAGE_TRANSFER_CREATED_SUCCESS),
					HttpStatus.CREATED);

		} catch (Exception e) {
			System.out.print(e);
			return new ResponseEntity<>(addErrorMessage(ERROR_MESSAGE_TRANSFER_UNAVAILABLE),
					HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}

	@RequestMapping(value = "api/transaction/withdrawal", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> createWithDrawal(@RequestBody TransactionDTO transacation) throws Exception {
		try {
			if (transacation.getValue() <= 0) {
				return new ResponseEntity<>(addErrorMessage(ERROR_MESSAGE_WITHDRAWAL_VALUE_NOT_VALID),
						HttpStatus.INTERNAL_SERVER_ERROR);

			}

			Double balanceOld = movimentacaoDetailsService.getBalance(transacation.getAccountId(), 1);
			if (balanceOld - transacation.getValue() < 0) {
				return new ResponseEntity<>(addErrorMessage(ERROR_MESSAGE_NO_MONEY_TO_THIS_TRANSACTION),
						HttpStatus.INTERNAL_SERVER_ERROR);

			}
			double value = (transacation.getValue());
			transacation.setValue(value*-1);
			transacation.setDescriptionStatus("Approved");
			transacation.setStatusTransaction(1);
			transacation.setType_operation("Debit");
			transacation.setMoney_name("BRL");
			transacation.setTypeTransactionId(1);
			transacation.setDescription_type_transaction("Out - Withdrawal");
			transacation.setMoney_simbol("R$");

			movimentacaoDetailsService.save(transacation);
			Double balanceNew = movimentacaoDetailsService.getBalance(transacation.getAccountId(), 1);
			System.out.print("old"+balanceOld);
			System.out.print("new"+balanceNew);
			
			System.out.print("t "+transacation.getValue());
			
			if ((balanceOld - value != balanceNew)) {
				return new ResponseEntity<>(addErrorMessage(ERROR_MESSAGE_WRONG_BALANCE_AFTER_TRANSACTION),
						HttpStatus.INTERNAL_SERVER_ERROR);
			}

			return new ResponseEntity<>(addFeedbackMessage(FEEDBACK_MESSAGE_WITHDRAWAL_CREATED), HttpStatus.CREATED);

		} catch (Exception e) { 
			return new ResponseEntity<>(addErrorMessage(ERROR_MESSAGE_WITHDRAWAL_UNAVAILABLE),
					HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}

	@RequestMapping(value = "api/transaction/extract", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getExtrato(@RequestBody TransactionDTO transacation) throws Exception {
		try {
			int contaCorrenteId = 1;
			int contaPoupancaId = 2;

			List<TransactionDao> transactionsContaCorrente = movimentacaoDetailsService
					.searchByAccountId(contaCorrenteId, 1);

			Extrato extratoContaCorrente = new Extrato("Conta Poupança", "BRL", "R$", transactionsContaCorrente);

			List<TransactionDao> transactionsContaPoupanca = movimentacaoDetailsService
					.searchByAccountId(contaPoupancaId, 1);
			Extrato extratoContaPoupanca = new Extrato("Conta Poupança", "BRL", "R$", transactionsContaPoupanca);

			List<Extrato> listaExtrato = new ArrayList<Extrato>();
			listaExtrato.add(extratoContaPoupanca);
			listaExtrato.add(extratoContaCorrente);
			return new ResponseEntity<>(addFeedbackMessage(FEEDBACK_MESSAGE_ALL_TRANSACTIONS_AVAILABLE, listaExtrato),
					HttpStatus.OK);
		} catch (Exception e) {

			return new ResponseEntity<>(addErrorMessage(ERROR_MESSAGE_ALL_TRANSACTIONS_NOT_AVAILABLE),
					HttpStatus.INTERNAL_SERVER_ERROR);

		}
	}

}
