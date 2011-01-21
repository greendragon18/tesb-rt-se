/*******************************************************************************
 * Copyright (c) 2010 SOPERA GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     SOPERA GmbH - initial API and implementation
 *******************************************************************************/
package org.talend.esb.client.model;

import org.sopera.services.crm.types.CustomerDetailsType;
import org.sopera.services.reservation.types.ConfirmationType;
import org.sopera.services.reservation.types.RESCarType;
import org.sopera.services.reservation.types.RESStatusType;

public interface CarReserveModel {
	public RESStatusType reserveCar(CustomerDetailsType customer, RESCarType car, String pickupDate, String returnDate);

	public ConfirmationType getConfirmation(RESStatusType reservationState, CustomerDetailsType customer,
			RESCarType car, String pickupDate, String returnDate);
}
