package se.grouprich.webshop.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "`Order`")
@NamedQueries(value = { @NamedQuery(name = "Order.FetchAll", query = "SELECT o FROM Order o"),
		@NamedQuery(name = "Order.FetchOrdersByUser", query = "SELECT o FROM Order o JOIN FETCH o.user u WHERE u.id = :id"),
		@NamedQuery(name = "Order.FetchOrdersByStatus", query = "SELECT o FROM Order o WHERE o.status = :status"),
		@NamedQuery(name = "Order.FetchOrdersByMinimumValue", query = "SELECT o FROM Order o WHERE o.totalPrice >= :totalPrice") })
public class Order extends AbstractEntity implements Serializable
{
	@Transient
	private static final long serialVersionUID = 3380539865925002167L;

	@JoinColumn(nullable = false)
	@OneToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	private User user;

	@Column(nullable = false)
	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE }, fetch = FetchType.EAGER)
	private List<OrderRow> orderRows;

	@Column(nullable = false)
	private Double totalPrice;

	@Column(nullable = false)
	private String status;

	public Order()
	{
	}

	public Order(User user)
	{
		this.user = user;
		orderRows = new ArrayList<>();
		status = "Placed";
		calculateTotalPrice();
	}

	public User getUser()
	{
		return user;
	}

	public List<OrderRow> getOrderRows()
	{
		return orderRows;
	}

	public Double getTotalPrice()
	{
		return totalPrice;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public Order addOrderRow(OrderRow orderRow)
	{
		OrderRow orderRowWithExistingProduct = searchProductInOrderRows(orderRow);
		if (orderRowWithExistingProduct != null)
		{
			addQuantity(orderRowWithExistingProduct, orderRow.getQuantity());
		}
		else
		{
			orderRows.add(orderRow);
		}
		calculateTotalPrice();
		return this;
	}

	public void calculateTotalPrice()
	{
		Double totalPrice = 0.0;
		for (OrderRow orderRow : orderRows)
		{
			totalPrice += orderRow.getProduct().getPrice() * orderRow.getQuantity();
		}
		BigDecimal bd = new BigDecimal(totalPrice);
		bd = bd.setScale(2, RoundingMode.HALF_UP);
		this.totalPrice = bd.doubleValue();
	}

	public void addQuantity(OrderRow orderRow, int quantity)
	{
		orderRow.setQuantity(orderRow.getQuantity() + quantity);
	}

	public OrderRow searchProductInOrderRows(OrderRow orderRow)
	{
		for (OrderRow orderRowInOrderRows : orderRows)
		{
			if (orderRowInOrderRows.getProduct().getId().equals(orderRow.getProduct().getId()))
			{
				return orderRowInOrderRows;
			}
		}
		return null;
	}

	@Override
	public boolean equals(Object other)
	{
		if (this == other)
		{
			return true;
		}

		if (other instanceof Order)
		{
			Order otherOrder = (Order) other;
			return user.equals(otherOrder.user) && orderRows.equals(otherOrder.orderRows) && totalPrice.equals(otherOrder.totalPrice)
					&& status.equals(otherOrder.status);
		}
		return false;
	}

	@Override
	public int hashCode()
	{
		int result = 1;
		result += user.hashCode() * 37;
		result += orderRows.hashCode() * 37;
		result += totalPrice.hashCode() * 37;
		result += status.hashCode() * 37;

		return result;
	}

	@Override
	public String toString()
	{
		return "Order [user=" + user.getUsername() + ", orderRows=" + orderRows + ", totalPrice=" + totalPrice + ", status=" + status + "]";
	}
}
