/*
 *
 *  * Copyright (c) 2013-2017 Atlanmod INRIA LINA Mines Nantes.
 *  * All rights reserved. This program and the accompanying materials
 *  * are made available under the terms of the Eclipse Public License v1.0
 *  * which accompanies this distribution, and is available at
 *  * http://www.eclipse.org/legal/epl-v10.html
 *  *
 *  * Contributors:
 *  *     Atlanmod INRIA LINA Mines Nantes - initial API and implementation
 *
 *
 */

package org.atlanmod.consistency.message;


import org.atlanmod.consistency.core.Id;
import org.atlanmod.consistency.core.NodeId;
import org.eclipse.emf.ecore.EClass;

import java.io.Serializable;

public interface UpdateMessage extends Serializable {

    MessageType type();

    Id instanceId();

    Id featureId();

    Object value();

    Object oldValue();

    Id reference();

    Id oldReference();

    EClass getEClass();

    NodeId getOriginator();
}
