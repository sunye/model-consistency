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

import com.google.common.primitives.Ints;
import org.atlanmod.commons.log.Log;
import org.atlanmod.consistency.History;
import org.atlanmod.consistency.adapter.EObjectAdapter;
import org.atlanmod.consistency.core.*;
import org.atlanmod.consistency.util.ConsistencyUtil;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.atlanmod.consistency.util.ConsistencyUtil.*;

public class ChangeManager {

    private final History history;
    private final NodeId nid;


    public ChangeManager(History history) {
        this.history = history;
        this.nid = history.getResource().getParentNid();
    }



    public void notifyChanged(InstanceId oid, Notification notification) {
        assert nonNull(notification);
        assert nonNull(notification.getNotifier());
        assert EObject.class.isAssignableFrom(notification.getNotifier().getClass());

        if (notification.isTouch()) {return;}
        if(isNull(notification.getFeature())) {return;}
        int type = notification.getEventType();
        Operation op;

        switch (type) {
            case Notification.SET :
                op = set(oid, notification);
                history.add(op);
                break;
            case Notification.UNSET:
                op = unset(oid, notification);
                history.add(op);
                break;
            case Notification.ADD:
                op = add(oid, notification);
                history.add(op);
                break;
            case Notification.REMOVE:
                op = remove(oid, notification);
                history.add(op);
                break;
            case Notification.MOVE:
                op = move(oid, notification);
                history.add(op);
                break;
            case Notification.ADD_MANY:
                op = addMany(oid, notification);
                history.add(op);
                break;
            case Notification.REMOVE_MANY:
                op = removeMany(oid, notification);
                history.add(op);
                break;
            case Notification.REMOVING_ADAPTER:
                Log.info("--removing adapter--");
                break;
            case Notification.NO_FEATURE_ID: break;
            case Notification.RESOLVE: break;

            default: break;
        }

    }

    private Operation set(InstanceId oid, Notification notification) {
        assert nonNull(notification.getFeature()) : "Set of a null feature";
        //assert nonNull(notification.getNewValue()) : "Set with a null value";

        if (Objects.isNull(notification.getNewValue())) {
            return unset(oid, notification);
        }

        EStructuralFeature feature = (EStructuralFeature) notification.getFeature();
        FeatureId fid = oid.withFeature(feature);


        notification.wasSet();
        notification.isReset();

        if (isEAttribute(feature)) {
            return new SetValue(fid, notification.getNewValue(), notification.getOldValue(), nid);
        } else if (isEReference(feature)) {
            return new SetReference(fid, identifierFor((EObject) notification.getNewValue()), nid);
        } else {
            return new Invalid(nid);
        }
    }

    private Operation unset(InstanceId oid, Notification notification) {
        assert nonNull(notification.getFeature()) : "Unset of a null feature";

        EStructuralFeature feature = (EStructuralFeature) notification.getFeature();
        FeatureId fid = oid.withFeature(feature);
        return new Unset(fid, nid);
    }

    private Operation add(InstanceId oid, Notification notification) {
        assert nonNull(notification.getFeature()) : "Add of a null feature";
        assert nonNull(notification.getNewValue()) : "Add with a null value";

        EStructuralFeature feature = (EStructuralFeature) notification.getFeature();
        FeatureId fid = oid.withFeature(feature);

        if (isEAttribute(feature)) {
            return new AddValue(fid, notification.getNewValue(), nid);
        } else if (isEReference(feature)) {
            return new AddReference(fid, identifierFor((EObject) notification.getNewValue()), nid);
        } else {
            return new Invalid(nid);
        }
    }

    private Operation remove(InstanceId oid, Notification notification) {
        assert nonNull(notification.getFeature()) : "Remove of a null feature";
        assert nonNull(notification.getOldValue()) : "Remove with a null old value";

        EStructuralFeature feature = (EStructuralFeature) notification.getFeature();
        FeatureId fid = oid.withFeature(feature);

        if (isEAttribute(feature)) {
            return new RemoveValue(fid, notification.getOldValue(), nid);
        } else if (isEReference(feature)) {
            Id newOid = identifierFor((EObject) notification.getOldValue());
            if (isNull(newOid)) {
                newOid = history.getResource().getDetachments().stream()
                        .filter(EObjectAdapter.class::isInstance)
                        .map(EObjectAdapter.class::cast)
                        .findFirst().orElse(null).id();
            }
            return new RemoveReference(fid, newOid, nid);
        } else {
            return new Invalid(nid);
        }
    }

    private Operation move(InstanceId oid, Notification notification) {
        assert nonNull(notification.getFeature()) : "Move of a null feature";
        Log.info("{0}", notification.toString());

        EStructuralFeature feature = (EStructuralFeature) notification.getFeature();
        FeatureId fid = oid.withFeature(feature);
        return new MoveValue(fid, notification.getOldValue(), notification.getPosition(), nid);
    }

    private Operation addMany(InstanceId oid, Notification notification) {
        assert nonNull(notification.getFeature()) : "AddMany of a null feature";
        assert nonNull(notification.getNewValue()) : "AddMany with a null value";

        EStructuralFeature feature = (EStructuralFeature) notification.getFeature();
        FeatureId fid = oid.withFeature(feature);

        if (isEAttribute(feature)) {
            List<Object> values = (List<Object>) notification.getNewValue();
             return new AddManyValues(fid, values, nid);
        } else if (isEReference(feature)) {
            List<EObject> values = (List<EObject>) notification.getNewValue();
            List<Id> ids = values.stream()
                    .map(ConsistencyUtil::identifierFor)
                    .collect(Collectors.toList());

            return new AddManyReferences(fid, ids, nid);
        } else {
            return new Invalid(nid);
        }
    }

    private Operation removeMany(InstanceId oid, Notification notification) {
        assert nonNull(notification.getFeature()) : "RemoveMany of a null feature";
        //assert nonNull(notification.getNewValue()) : "RemoveMany with a null value"; -- CLEAR

        EStructuralFeature feature = (EStructuralFeature) notification.getFeature();
        FeatureId fid = oid.withFeature(feature);

        if (notification.getNewValue() == null) { // clear() case
            if (isEReference(feature)) {

                List<Id> values = new ArrayList<>();
                for (EObject obj : ((EStructuralFeature) notification.getFeature()).eContents()) {
                    values.add(identifierFor(obj));
                }

                return new RemoveManyReferences(fid, values, nid);

            } else if (isEAttribute(feature)) {

                List<Object> values = new ArrayList<>(((EStructuralFeature) notification.getFeature()).eContents());
                return new RemoveManyValues(fid,values, nid);

            } else {
                return new Invalid(nid);
            }
        } else {

            if (isEAttribute(feature)) {
                return new RemoveManyValues(fid, (List<Object>) notification.getOldValue(), nid);
            } else if (isEReference(feature)) {

                List<Integer> values = Ints.asList((int[]) notification.getNewValue());

                List<Id> ids = new ArrayList<>();
                for (Integer i : values) {
                    ids.add(IdBuilder.fromInt(i));
                }
                return new RemoveManyReferences(fid, ids, nid);
            } else {
                return new Invalid(nid);
            }
        }
    }

}
