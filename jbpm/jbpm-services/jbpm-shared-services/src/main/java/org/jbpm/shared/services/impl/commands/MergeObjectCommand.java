/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.jbpm.shared.services.impl.commands;

import org.drools.core.command.impl.GenericCommand;
import org.jbpm.shared.services.impl.JpaPersistenceContext;
import org.kie.internal.command.Context;

public class MergeObjectCommand implements GenericCommand<Void> {

	private static final long serialVersionUID = -4014807273522465028L;

	private Object[] objectsToMerge;

	public MergeObjectCommand(Object ...objects) {
		this.objectsToMerge = objects;
	}
	
	@Override
	public Void execute(Context context) {
		JpaPersistenceContext ctx = (JpaPersistenceContext) context;
		if (objectsToMerge != null) {
			for (Object object : objectsToMerge) {
				ctx.merge(object);
			}
		}
		return null;
	}

}
