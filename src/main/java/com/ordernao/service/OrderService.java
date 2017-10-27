package com.ordernao.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ordernao.bean.EmployeeBean;
import com.ordernao.bean.OrderBean;
import com.ordernao.dao.OrderDao;
import com.ordernao.utility.OrderNaoConstants;
import com.ordernao.utility.Utility;

@Service
public class OrderService {
	static final Logger logger = LogManager.getLogger(OrderService.class.getName());
	@Autowired
	OrderDao dao;

	public OrderBean checkExistingCustomer(String contactNumber) {
		logger.info("Entry at checkExistingCustomer(Service)");
		OrderBean orderBean = new OrderBean();
		try {
			int result = dao.checkExistingCustomer(contactNumber);
			if (result > OrderNaoConstants.CUSTOMER_EXIST) {
				logger.info("Customer exist fetching customer details");
				orderBean = dao.getExistingCustomerInfo(contactNumber);
				orderBean.setNewCustomer(false);
			} else {
				logger.info("Customer doesn't exist");
				orderBean.setNewCustomer(true);
			}
			logger.info("Exit at checkExistingCustomer(Service)");
			return orderBean;
		} catch (Exception e) {
			logger.error("Exception :- ", e);
			return orderBean;
		}

	}

	public boolean saveNewCustomer(OrderBean orderBean) {
		logger.info("Entry at saveNewCustomer(Service)");
		boolean status = false;
		int customerStatus = dao.saveNewCustomerInfo(orderBean);
		if (customerStatus > 0) {
			status = insertOrderDetails(orderBean);
		}
		logger.info("Exit at saveNewCustomer(Service)");
		return status;
	}

	public boolean insertOrderDetails(OrderBean orderBean) {
		logger.info("Entry at insertOrderDetails(Service)");
		boolean status = false;
		String contactNumber = orderBean.getCustomerPhone();
		int id = dao.getCustomerIdFromContactNumber(contactNumber);
		orderBean.setCustomerId(id);
		String comments = orderBean.getComments();
		comments.replaceAll("\\s", "");
		if (!comments.isEmpty()) {
			logger.info("Comment Status :- Failed");
			orderBean.setCommentStatus(OrderNaoConstants.COMMENT_STATUS_FAILED);
		} else {
			logger.info("Comment Status :- N/A");
		}
		int orderStatus1 = dao.saveNewOrderInfoForCustomerInOrdersTable(orderBean);
		if (orderStatus1 > 0) {
			int orderNumber = dao.getOrderNumberFromClientId(id);
			int totalOrders = orderBean.getTotalNumberOfOrders();
			String[] itemNamesArray = Utility.getArrayFromString(orderBean.getItemName());
			String[] orderPickupPointsArray = Utility.getArrayFromString(orderBean.getOrderPickedFrom());
			String[] orderDeliveredAtArray = Utility.getArrayFromString(orderBean.getOrderDeliveredAt());
			if (totalOrders > 1) {
				// Here total order is number of pickup points and delivery
				// points
				// Multiple if totalOrders is > 1 else only one
				int[] mulipleOrderStatus = dao.saveMultipleOrders(orderBean, orderNumber, itemNamesArray,
						orderPickupPointsArray, orderDeliveredAtArray);
				if (mulipleOrderStatus.length > 1) {
					status = true;
				} else {
					status = false;
				}
			} else {
				int singleOrderStatus = dao.saveNewOrderInfoForCustomerInOrderDetailsTable(orderBean, orderNumber,
						itemNamesArray, orderPickupPointsArray, orderDeliveredAtArray);
				if (singleOrderStatus > 0) {
					status = true;
				} else {
					status = false;
				}
			}
		} else {
			status = false;
		}
		logger.info("Exit at saveNewCustomer(Service)");
		return status;
	}

	public OrderBean getNewCustomerInfo(String contactNumber) {
		logger.info("Entry at getNewCustomerInfo(Service)");
		logger.info("Exit at getNewCustomerInfo(Service)");
		return dao.getNewCustomerInfo(contactNumber);
	}

	public boolean saveEmployee(EmployeeBean employee) {
		logger.info("Entry at saveEmployee(Service)");
		boolean status = false;
		String employeeType = employee.getEmployeeType();
		logger.info("Employee Type :-(1-admin)(2-manager)(3-call operator)(4-delivery boy) " + employeeType);
		int employeeTypeId = OrderNaoConstants.INVALID_EMPLOYEE_TYPE;
		if (employeeType.equals(OrderNaoConstants.ADMIN)) {
			employeeTypeId = OrderNaoConstants.ADMIN_TYPE;
		} else if (employeeType.equals(OrderNaoConstants.MANAGER)) {
			employeeTypeId = OrderNaoConstants.MANAGER_TYPE;
		} else if (employeeType.equals(OrderNaoConstants.CALL_OPERATOR)) {
			employeeTypeId = OrderNaoConstants.CALL_OPERATOR_TYPE;
		} else if (employeeType.equals(OrderNaoConstants.DELIVERY_BOY)) {
			employeeTypeId = OrderNaoConstants.DELIVERY_BOY_TYPE;
		}

		try {
			int result = dao.saveEmployee(employee, employeeTypeId);
			if (result > 0) {
				logger.info("Employee inserted Successfully...");
				status = true;
			} else {
				status = false;
			}
			logger.info("Exit at saveEmployee(Service)");
			return status;
		} catch (Exception e) {
			logger.error("Exception :- " + e);
			return status;
		}

	}

	public boolean checkForUserName(String userName) {
		logger.info("Entry at checkForUserName(Service)");
		boolean status = false;
		try {
			int result = dao.checkForUserName(userName);
			logger.info("checkForUserName :- " + result);
			if (result > 0) {
				status = true;
			} else {
				status = false;
			}
			logger.info("Exit at checkForUserName(Service)");
			return status;
		} catch (Exception e) {
			logger.error("Exception " + e);
			return true;
		}

	}

	public List<OrderBean> getOrderDetailsOfCustomer() {
		return dao.getOrderDetailsOfCustomer();
	}

	public List<OrderBean> getDeliveryBoysList() {
		return dao.getDeliveryBoysList();
	}

	/*
	 * public boolean assignOrderToDeliveryBoy(int deliveryBoyId, int
	 * orderNumber) { logger.info("Entry at assignOrderToDeliveryBoy(Service)");
	 * boolean status = false; int result =
	 * dao.updateDeliveryBoyInOrdersTable(deliveryBoyId, orderNumber); if
	 * (result > 0) { status = true; }
	 * logger.info("Exit at assignOrderToDeliveryBoy(Service)"); return status;
	 * }
	 */

	public List<OrderBean> getMoreDetailOfOrder(int orderNumber) {
		logger.info("Entry at getMoreDetailOfOrder(Service)");
		List<OrderBean> moreDetailsOfOrderList = new ArrayList<OrderBean>();
		try {
			logger.info("Exit at getMoreDetailOfOrder(Service)");
			moreDetailsOfOrderList = dao.getMoreDetailOfOrder(orderNumber);
			return moreDetailsOfOrderList;
		} catch (Exception e) {
			logger.error("Exception :- " + e);
			return moreDetailsOfOrderList;
		}

	}

	/*
	 * public List<OrderBean> filterOrdersByStatus(String status) {
	 * logger.info("Entry at filterOrdersByStatus(Service)"); List<OrderBean>
	 * filteredOrderList = new ArrayList<OrderBean>(); try {
	 * logger.info("Exit at filterOrdersByStatus(Service)"); filteredOrderList =
	 * dao.filterOrdersByStatus(status); return filteredOrderList;
	 * 
	 * } catch (Exception e) { logger.error("Exception :- " + e); return
	 * filteredOrderList; } }
	 * 
	 * public List<OrderBean> filterOrdersByDate(String date) {
	 * logger.info("Entry at filterOrdersByDate(Service)"); List<OrderBean>
	 * filteredOrderList = new ArrayList<OrderBean>(); try {
	 * logger.info("Exit at filterOrdersByDate(Service)"); filteredOrderList =
	 * dao.filterOrdersByDate(date); return filteredOrderList;
	 * 
	 * } catch (Exception e) { logger.error("Exception :- " + e); return
	 * filteredOrderList; } }
	 * 
	 * public List<OrderBean> filterOrdersByAssignment(String assignment) {
	 * logger.info("Entry at filterOrdersByAssignment(Service)");
	 * List<OrderBean> filteredOrderList = new ArrayList<OrderBean>(); try {
	 * logger.info("Exit at filterOrdersByAssignment(Service)");
	 * filteredOrderList = dao.filterOrdersByAssignment(assignment); return
	 * filteredOrderList;
	 * 
	 * } catch (Exception e) { logger.error("Exception :- " + e); return
	 * filteredOrderList; } }
	 */

	public List<OrderBean> searchTrackDelivery(String searchKey) {
		logger.info("Entry at searchTrackDelivery(Service)");
		List<OrderBean> searchOrderList = new ArrayList<OrderBean>();
		try {
			logger.info("Exit at searchTrackDelivery(Service)");
			searchOrderList = dao.searchTrackDelivery(searchKey);
			return searchOrderList;

		} catch (Exception e) {
			logger.error("Exception :- " + e);
			return searchOrderList;
		}
	}

	public OrderBean getCommentsOfFailedOrder(String orderNumber) {
		logger.info("Entry at getCommentsOfFailedOrder(Service)");
		OrderBean bean = dao.getCommentsOfFailedOrder(orderNumber);
		logger.info("Exit at getCommentsOfFailedOrder(Service)");
		return bean;
	}

	public boolean checkForOrderNumberAndFailedStatus(String orderNumber) {
		logger.info("Entry at checkForOrderNumberAndFailedStatus(Service)");
		int result = dao.checkForOrderNumberAndFailedStatus(orderNumber);
		if (result > 0) {
			logger.info("Exit at checkForOrderNumberAndFailedStatus(Service) :- Status Exist ");
			return true;
		} else {
			logger.info("Exit at checkForOrderNumberAndFailedStatus(Service) :- Status Doesn't Exist ");
			return false;
		}
	}

	/*
	 * public boolean checkForPendingOrDeliveredOrder(int orderNumber) {
	 * logger.info("Entry at checkForPendingOrFailedOrder(Service)"); int result
	 * = dao.checkForPendingOrDeliveredOrder(orderNumber); if (result > 0) {
	 * logger.info("Exit at checkForPendingOrFailedOrder(Service)"); return
	 * true; } else {
	 * logger.info("Exit at checkForPendingOrFailedOrder(Service)"); return
	 * false; } }
	 */

	/*
	 * public boolean saveFailedStatusCommentsOfOrder(String orderNumber, String
	 * failedComments) {
	 * logger.info("Entry at saveFailedStatusCommentsOfOrder(Service)"); int
	 * result = dao.saveFailedStatusCommentsOfOrder(orderNumber,
	 * failedComments); if (result > 0) {
	 * logger.info("Exit at saveFailedStatusCommentsOfOrder(Service)"); return
	 * true; } else {
	 * logger.info("Exit at saveFailedStatusCommentsOfOrder(Service)"); return
	 * false; } }
	 */

	/*
	 * public boolean savePendingOrFailedOrderStatus(String orderNumber, String
	 * newStatusOfOrder) {
	 * logger.info("Entry at savePendingOrFailedOrderStatus(Service)"); int
	 * result = dao.savePendingOrFailedOrderStatus(orderNumber,
	 * newStatusOfOrder); if (result > 0) {
	 * logger.info("Exit at savePendingOrFailedOrderStatus(Service)"); return
	 * true; } else {
	 * logger.info("Exit at savePendingOrFailedOrderStatus(Service)"); return
	 * false; } }
	 */

	/*
	 * public boolean checkDeliveryBoyIdInDB(int deliveryBoyId) {
	 * logger.info("Entry at savePendingOrFailedOrderStatus(Service)"); int
	 * result = dao.checkDeliveryBoyIdInDB(deliveryBoyId); if (result > 0) {
	 * logger.info("Exit at checkDeliveryBoyIdInDB(Service)"); return true; }
	 * else { logger.info("Exit at checkDeliveryBoyIdInDB(Service)"); return
	 * false; } }
	 * 
	 * public List<OrderBean> filterOrdersByStatusAndTime(String date, String
	 * status) { logger.info("Entry at filterOrdersByStatusAndTime(Service)");
	 * List<OrderBean> filteredOrderList = new ArrayList<OrderBean>(); try {
	 * logger.info("Exit at filterOrdersByStatusAndTime(Service)");
	 * filteredOrderList = dao.filterOrdersByStatusAndTime(date, status); return
	 * filteredOrderList; } catch (Exception e) { logger.error("Exception :- " +
	 * e); return filteredOrderList; } }
	 * 
	 * public List<OrderBean> filterOrdersByTimeAndAssignment(String time,
	 * String assignment) {
	 * logger.info("Entry at filterOrdersByTimeAndAssignment(Service)");
	 * List<OrderBean> filteredOrderList = new ArrayList<OrderBean>(); try {
	 * logger.info("Exit at filterOrdersByTimeAndAssignment(Service)");
	 * filteredOrderList = dao.filterOrdersByTimeAndAssignment(time,
	 * assignment); return filteredOrderList; } catch (Exception e) {
	 * logger.error("Exception :- " + e); return filteredOrderList; } }
	 * 
	 * public List<OrderBean> filterOrdersByStatusAndAssignment(String status,
	 * String assignment) {
	 * logger.info("Entry at filterOrdersByStatusAndAssignment(Service)");
	 * List<OrderBean> filteredOrderList = new ArrayList<OrderBean>(); try {
	 * logger.info("Exit at filterOrdersByStatusAndAssignment(Service)");
	 * filteredOrderList = dao.filterOrdersByStatusAndAssignment(status,
	 * assignment); return filteredOrderList; } catch (Exception e) {
	 * logger.error("Exception :- " + e); return filteredOrderList; } }
	 * 
	 * public List<OrderBean> filterOrdersByStatusAndTimeAndAssignment(String
	 * status, String time, String assignment) {
	 * logger.info("Entry at filterOrdersByStatusAndTimeAndAssignment(Service)"
	 * ); List<OrderBean> filteredOrderList = new ArrayList<OrderBean>(); try {
	 * logger.info("Exit at filterOrdersByStatusAndTimeAndAssignment(Service)");
	 * filteredOrderList = dao.filterOrdersByStatusAndTimeAndAssignment(status,
	 * time, assignment); return filteredOrderList; } catch (Exception e) {
	 * logger.error("Exception :- " + e); return filteredOrderList; } }
	 */

	public List<OrderBean> filterTrackOrder(String status, String date, String assignment, String searchKey) {
		logger.info("Entry at filterTrackOrder(Service)");

		if ((status != null && status.trim().length() > 0)
				&& (status.equalsIgnoreCase(OrderNaoConstants.ORDER_STATUS_PENDING)
						|| status.equalsIgnoreCase(OrderNaoConstants.ORDER_STATUS_DELIVERED)
						|| status.equalsIgnoreCase(OrderNaoConstants.ORDER_STATUS_FAILED))) {

			if ((date != null && date.trim().length() > 0) && (date.equalsIgnoreCase(OrderNaoConstants.ORDER_DATE_TODAY)
					|| date.equalsIgnoreCase(OrderNaoConstants.ORDER_DATE_LAST_SEVEN_DAYS)
					|| date.equalsIgnoreCase(OrderNaoConstants.ORDER_DATE_THIS_MONTH)
					|| date.equalsIgnoreCase(OrderNaoConstants.ORDER_DATE_TILL_DATE))) {

				if ((assignment != null && assignment.trim().length() > 0)
						&& (assignment.equalsIgnoreCase(OrderNaoConstants.ORDER_ASSIGNED)
								|| assignment.equalsIgnoreCase(OrderNaoConstants.ORDER_UNASSIGNED))) {
					logger.info("Process request for status & date & assignment(Service)");
					// Process request for status & date & assignment
					logger.info("Exit at filterTrackOrder(Service)");
					return dao.filterOrdersByStatusAndTimeAndAssignment(status, date, assignment);
				}
				logger.info("Process request for status & date(Service)");
				// Process request for status & date
				logger.info("Exit at filterTrackOrder(Service)");
				return dao.filterOrdersByStatusAndTime(date, status);
			} else if ((assignment != null && assignment.trim().length() > 0)
					&& (assignment.equalsIgnoreCase(OrderNaoConstants.ORDER_ASSIGNED)
							|| assignment.equalsIgnoreCase(OrderNaoConstants.ORDER_UNASSIGNED))) {
				logger.info("Process request for status & assignment(Service)");
				// Process request for status & assignment
				logger.info("Exit at filterTrackOrder(Service)");
				return dao.filterOrdersByStatusAndAssignment(status, assignment);

			}
			logger.info("Process request for status(Service)");
			// Process request for status
			logger.info("Exit at filterTrackOrder(Service)");
			return dao.filterOrdersByStatus(status);

		} else if ((date != null && date.trim().length() > 0)
				&& (date.equalsIgnoreCase(OrderNaoConstants.ORDER_DATE_TODAY)
						|| date.equalsIgnoreCase(OrderNaoConstants.ORDER_DATE_LAST_SEVEN_DAYS)
						|| date.equalsIgnoreCase(OrderNaoConstants.ORDER_DATE_THIS_MONTH)
						|| date.equalsIgnoreCase(OrderNaoConstants.ORDER_DATE_TILL_DATE))) {

			if ((assignment != null && assignment.trim().length() > 0)
					&& (assignment.equalsIgnoreCase(OrderNaoConstants.ORDER_ASSIGNED)
							|| assignment.equalsIgnoreCase(OrderNaoConstants.ORDER_UNASSIGNED))) {
				logger.info("Process request for date & assignment(Service)");
				// Process request for date & assignment
				logger.info("Exit at filterTrackOrder(Service)");
				return dao.filterOrdersByTimeAndAssignment(date, assignment);

			}
			logger.info("Process request for date(Service)");
			// Process request for date
			logger.info("Exit at filterTrackOrder(Service)");
			return dao.filterOrdersByDate(date);

		} else if ((assignment != null && assignment.trim().length() > 0)
				&& (assignment.equalsIgnoreCase(OrderNaoConstants.ORDER_ASSIGNED)
						|| assignment.equalsIgnoreCase(OrderNaoConstants.ORDER_UNASSIGNED))) {
			logger.info("Process request for assignment(Service)");
			// process request for only assignment

			logger.info("Exit at filterTrackOrder(Service)");
			return dao.filterOrdersByAssignment(assignment);
		} else if (searchKey != null && searchKey.trim().length() > 0) {
			// this is used to process request in case of search
			logger.info("Process request for search :- " + searchKey);
			logger.info("Exit at ");
			return dao.searchTrackDelivery(searchKey);

		} else {
			// no condition is satisfied so we show default page
			logger.info("Processing default track delivery page (Service)");
			logger.info("Exit at filterTrackOrder(Service)");
			return dao.getOrderDetailsOfCustomer();
		}
	}

	public String savePendingOrDeliveredOrFailedOrder(int orderNumber, String status, String failedComments) {
		logger.info("Entry at savePendingOrDeliveredOrder(Service)");
		if ((status != null && status.trim().length() > 0)
				&& (status.equalsIgnoreCase(OrderNaoConstants.ORDER_STATUS_DELIVERED)
						|| status.equalsIgnoreCase(OrderNaoConstants.ORDER_STATUS_PENDING)
						|| status.equalsIgnoreCase(OrderNaoConstants.ORDER_STATUS_FAILED))) {
			// Process when status is not null
			int orderStatus = dao.checkForPendingOrDeliveredOrder(orderNumber);
			if (orderStatus > 0) {
				int newOrderStatus = 0;
				if (status.equalsIgnoreCase(OrderNaoConstants.ORDER_STATUS_DELIVERED)
						|| status.equalsIgnoreCase(OrderNaoConstants.ORDER_STATUS_PENDING)) {
					newOrderStatus = dao.savePendingOrDeliveredOrderStatus(orderNumber, status);
					logger.info("Saving Pending Or Delivered order status ");
				} else if (status.equalsIgnoreCase(OrderNaoConstants.ORDER_STATUS_FAILED)) {
					newOrderStatus = dao.saveFailedStatusCommentsOfOrder(orderNumber, failedComments);
					logger.info("Saving Failed order status ");
				}

				if (newOrderStatus > 0) {
					logger.info("Exit at savePendingOrDeliveredOrder(Service) :Success");
					return OrderNaoConstants.RETURN_STATUS_SUCCESS;
				} else {
					logger.info("Exit at savePendingOrDeliveredOrder(Service) :Error");
					return OrderNaoConstants.RETURN_STATUS_ERROR;
				}
			} else {
				logger.info("Exit at savePendingOrDeliveredOrder(Service) :SuspiciousActivity");
				return OrderNaoConstants.RETURN_STATUS_SUSPICIOUS_ACTIVITY;
			}

		} else {
			logger.info("Exit at savePendingOrDeliveredOrder(Service) :SuspiciousActivity");
			return OrderNaoConstants.RETURN_STATUS_SUSPICIOUS_ACTIVITY;
		}
	}

	public String deliveryBoyOrderAssignment(int deliveryBoyId, int orderNumber) {
		logger.info("Entry at deliveryBoyOrderAssignment(Service) ");
		// here we check for id of delivery boy in db(for suspicious activity
		// i.e it is used to check if someone try to assign manager to a
		// particular order)
		int deliveryBoyStatus = dao.checkDeliveryBoyIdInDB(deliveryBoyId);
		if (deliveryBoyStatus > 0) {
			int deliveryBoysUpdate = dao.updateDeliveryBoyInOrdersTable(deliveryBoyId, orderNumber);
			if (deliveryBoysUpdate > 0) {
				logger.info("Exit at deliveryBoyOrderAssignment(Service) :success ");
				return OrderNaoConstants.RETURN_STATUS_SUCCESS;
			} else {
				logger.info("Exit at deliveryBoyOrderAssignment(Service) :error");
				return OrderNaoConstants.RETURN_STATUS_ERROR;
			}
		} else {
			logger.info("Exitat deliveryBoyOrderAssignment(Service) :suspicious activity");
			return OrderNaoConstants.RETURN_STATUS_SUSPICIOUS_ACTIVITY;
		}
	}

}
