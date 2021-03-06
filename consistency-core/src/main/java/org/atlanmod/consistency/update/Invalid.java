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

package org.atlanmod.consistency.update;

import org.atlanmod.consistency.core.Id;
import org.atlanmod.consistency.core.NodeId;

/**
 * Created on 09/03/2017.
 *
 * @author AtlanMod team.
 */
public class Invalid extends BaseOperation {
    public Invalid(NodeId originator) {
        super(originator);
    }

    @Override
    public Id instanceId() {
        return null;
    }
}
