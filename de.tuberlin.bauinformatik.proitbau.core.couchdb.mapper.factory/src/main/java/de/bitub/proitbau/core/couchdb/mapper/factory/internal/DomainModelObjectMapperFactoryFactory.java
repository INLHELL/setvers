package de.bitub.proitbau.core.couchdb.mapper.factory.internal;

import org.eclipse.core.runtime.IAdapterFactory;
import org.ektorp.util.Documents;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;

import de.bitub.proitbau.common.model.iModelObject;
import de.bitub.proitbau.common.model.iModelObjectMixIn;
import de.bitub.proitbau.common.model.iNamedModelObject;
import de.bitub.proitbau.common.model.iNamedModelObjectMixIn;
import de.bitub.proitbau.common.model.iProjectCalendar;
import de.bitub.proitbau.common.model.iProjectCalendarMixIn;
import de.bitub.proitbau.common.model.iUpdate;
import de.bitub.proitbau.common.model.iUpdateInformation;
import de.bitub.proitbau.common.model.bean.aObservable;
import de.bitub.proitbau.common.model.bean.aObservableMixIn;
import de.bitub.proitbau.common.model.datastructure.structuring.StructuringNode;
import de.bitub.proitbau.common.model.datastructure.structuring.StructuringNodeMixIn;
import de.bitub.proitbau.common.model.datastructure.structuring.StructuringObject;
import de.bitub.proitbau.common.model.datastructure.structuring.StructuringObjectMixIn;
import de.bitub.proitbau.common.model.datastructure.structuring.StructuringSet;
import de.bitub.proitbau.common.model.datastructure.structuring.StructuringSetMixIn;
import de.bitub.proitbau.common.model.datastructure.structuring.StructuringTree;
import de.bitub.proitbau.common.model.datastructure.structuring.StructuringTreeMixIn;
import de.bitub.proitbau.common.model.datastructure.structuring.iStructuringNode;
import de.bitub.proitbau.common.model.datastructure.structuring.iStructuringNodeMixIn;
import de.bitub.proitbau.common.model.datastructure.structuring.iStructuringObject;
import de.bitub.proitbau.common.model.datastructure.structuring.iStructuringObjectMixIn;
import de.bitub.proitbau.common.model.datastructure.structuring.iStructuringSet;
import de.bitub.proitbau.common.model.datastructure.structuring.iStructuringSetMixIn;
import de.bitub.proitbau.common.model.datastructure.structuring.iStructuringTree;
import de.bitub.proitbau.common.model.datastructure.structuring.iStructuringTreeMixIn;
import de.bitub.proitbau.common.model.persistance.iPersistableMixIn;
import de.bitub.proitbau.common.model.persistence.iPersistable;
import de.bitub.proitbau.common.model.templateinstance.iTemplateInstance;
import de.bitub.proitbau.common.model.templateinstance.iTemplateInstanceMixIn;
import de.bitub.proitbau.common.versioning.couchdb.binding.graph.BindingGraph;
import de.bitub.proitbau.common.versioning.couchdb.binding.graph.BindingGraphMixIn;
import de.bitub.proitbau.common.versioning.couchdb.binding.graph.VersionedSetRepresentation;
import de.bitub.proitbau.common.versioning.couchdb.binding.graph.VersionedSetRepresentationMixIn;
import de.bitub.proitbau.common.versioning.couchdb.binding.graph.VersionedSetWrapper;
import de.bitub.proitbau.common.versioning.couchdb.binding.graph.VersionedSetWrapperMixIn;
import de.bitub.proitbau.common.versioning.couchdb.binding.graph.iBindingGraph;
import de.bitub.proitbau.common.versioning.couchdb.binding.graph.iBindingGraphMixIn;
import de.bitub.proitbau.common.versioning.couchdb.mixin.document.accessor.BindingGraphDocumentAccessor;
import de.bitub.proitbau.common.versioning.couchdb.mixin.document.accessor.VersionedSetWrapperDocumentAccessor;
import de.bitub.proitbau.common.versioning.model.Versionable;
import de.bitub.proitbau.common.versioning.model.VersionableMixIn;
import de.bitub.proitbau.common.versioning.model.VersionedSet;
import de.bitub.proitbau.common.versioning.model.VersionedSetMixIn;
import de.bitub.proitbau.core.couchdb.mapper.factory.DomainModelObjectMapperFactory;
import de.bitub.proitbau.core.couchdb.mapper.factory.iDomainModelObjectMapperFactory;
import de.bitub.proitbau.core.couchdb.mapper.factory.mapper.DomainModelObjectMapper;
import de.bitub.proitbau.core.couchdb.mixin.document.accessor.DomainModelDocumentAccessor;
import de.bitub.proitbau.core.couchdb.mixin.document.accessor.ProjectDocumentAccessor;
import de.bitub.proitbau.core.model.domain.DomainModel;
import de.bitub.proitbau.core.model.domain.DomainModelMixIn;
import de.bitub.proitbau.core.model.domain.Update;
import de.bitub.proitbau.core.model.domain.UpdateInformation;
import de.bitub.proitbau.core.model.domain.UpdateInformationMixIn;
import de.bitub.proitbau.core.model.domain.UpdateMixIn;
import de.bitub.proitbau.core.model.domain.aChecklistedModelObject;
import de.bitub.proitbau.core.model.domain.aChecklistedModelObjectMixIn;
import de.bitub.proitbau.core.model.domain.aModelObject;
import de.bitub.proitbau.core.model.domain.aModelObjectMixIn;
import de.bitub.proitbau.core.model.domain.aNamedModelObject;
import de.bitub.proitbau.core.model.domain.aNamedModelObjectMixIn;
import de.bitub.proitbau.core.model.domain.aTemplateInstance;
import de.bitub.proitbau.core.model.domain.aTemplateInstanceMixIn;
import de.bitub.proitbau.core.model.domain.iDomainModel;
import de.bitub.proitbau.core.model.domain.iDomainModelMixIn;
import de.bitub.proitbau.core.model.domain.iUpdateInformationMixIn;
import de.bitub.proitbau.core.model.domain.iUpdateMixIn;
import de.bitub.proitbau.core.model.domain.activitystate.PDActivity;
import de.bitub.proitbau.core.model.domain.activitystate.PDActivityMixIn;
import de.bitub.proitbau.core.model.domain.activitystate.PIActivity;
import de.bitub.proitbau.core.model.domain.activitystate.PIActivityMixIn;
import de.bitub.proitbau.core.model.domain.activitystate.aActivity;
import de.bitub.proitbau.core.model.domain.activitystate.aActivityMixIn;
import de.bitub.proitbau.core.model.domain.activitystate.aActivityState;
import de.bitub.proitbau.core.model.domain.activitystate.aActivityStateMixIn;
import de.bitub.proitbau.core.model.domain.activitystate.aState;
import de.bitub.proitbau.core.model.domain.activitystate.aStateMixIn;
import de.bitub.proitbau.core.model.domain.process.PDProcess;
import de.bitub.proitbau.core.model.domain.process.PDProcessMixIn;
import de.bitub.proitbau.core.model.domain.process.PDSubprocess;
import de.bitub.proitbau.core.model.domain.process.PDSubprocessMixIn;
import de.bitub.proitbau.core.model.domain.process.PIProcess;
import de.bitub.proitbau.core.model.domain.process.PIProcessMixIn;
import de.bitub.proitbau.core.model.domain.process.PISubprocess;
import de.bitub.proitbau.core.model.domain.process.PISubprocessMixIn;
import de.bitub.proitbau.core.model.domain.process.aProcess;
import de.bitub.proitbau.core.model.domain.process.aProcessComponent;
import de.bitub.proitbau.core.model.domain.process.aProcessComponentMixIn;
import de.bitub.proitbau.core.model.domain.process.aProcessComposite;
import de.bitub.proitbau.core.model.domain.process.aProcessCompositeMixIn;
import de.bitub.proitbau.core.model.domain.process.aProcessMixIn;
import de.bitub.proitbau.core.model.domain.process.aSubprocess;
import de.bitub.proitbau.core.model.domain.process.aSubprocessMixIn;
import de.bitub.proitbau.core.model.domain.process.edge.SemanticEdge;
import de.bitub.proitbau.core.model.domain.process.edge.SemanticEdgeMixIn;
import de.bitub.proitbau.core.model.domain.process.node.aProcessNode;
import de.bitub.proitbau.core.model.domain.process.node.aProcessNodeMixIn;
import de.bitub.proitbau.core.model.domain.process.node.triple.PDTripleProcessNode;
import de.bitub.proitbau.core.model.domain.process.node.triple.PDTripleProcessNodeMixIn;
import de.bitub.proitbau.core.model.domain.process.node.triple.PITripleProcessNode;
import de.bitub.proitbau.core.model.domain.process.node.triple.PITripleProcessNodeMixIn;
import de.bitub.proitbau.core.model.domain.process.node.triple.aTripleProcessNode;
import de.bitub.proitbau.core.model.domain.process.node.triple.aTripleProcessNodeMixIn;
import de.bitub.proitbau.core.model.domain.structuring.StructuringNodePersistable;
import de.bitub.proitbau.core.model.domain.structuring.StructuringNodePersistableMixIn;
import de.bitub.proitbau.core.model.domain.structuring.StructuringObjectPersistable;
import de.bitub.proitbau.core.model.domain.structuring.StructuringObjectPersistableMixIn;
import de.bitub.proitbau.core.model.domain.structuring.StructuringSetPersistable;
import de.bitub.proitbau.core.model.domain.structuring.StructuringSetPersistableMixIn;
import de.bitub.proitbau.core.model.domain.structuring.StructuringTreePersistable;
import de.bitub.proitbau.core.model.domain.structuring.StructuringTreePersistableMixIn;
import de.bitub.proitbau.core.model.domain.subject.PDSubject;
import de.bitub.proitbau.core.model.domain.subject.PDSubjectMixIn;
import de.bitub.proitbau.core.model.domain.subject.PISubject;
import de.bitub.proitbau.core.model.domain.subject.PISubjectMixIn;
import de.bitub.proitbau.core.model.domain.subject.aSubject;
import de.bitub.proitbau.core.model.domain.subject.aSubjectMixIn;
import de.bitub.proitbau.core.model.domain.time.DailyShifts;
import de.bitub.proitbau.core.model.domain.time.DailyShiftsMixIn;
import de.bitub.proitbau.core.model.domain.time.ProjectCalendar;
import de.bitub.proitbau.core.model.domain.time.ProjectCalendarMixIn;
import de.bitub.proitbau.core.model.domain.time.Shift;
import de.bitub.proitbau.core.model.domain.time.ShiftMixIn;
import de.bitub.proitbau.core.model.domain.time.WeeklyShift;
import de.bitub.proitbau.core.model.domain.time.WeeklyShiftMixIn;
import de.bitub.proitbau.core.model.project.Project;
import de.bitub.proitbau.core.model.project.ProjectMixIn;

public class DomainModelObjectMapperFactoryFactory implements IAdapterFactory {
	
	final static Logger logger = (Logger) LoggerFactory.getLogger(DomainModelObjectMapperFactoryFactory.class);
	{
		DomainModelObjectMapperFactoryFactory.logger.setLevel(Level.INFO);
	}
	
	private iDomainModelObjectMapperFactory domainModelObjectMapperFactory;
	
	@Override
	public Object getAdapter(final Object adaptableObject, @SuppressWarnings("rawtypes") final Class adapterType) {
		if (adapterType.equals(iDomainModelObjectMapperFactory.class)) {
			if (this.domainModelObjectMapperFactory == null) {
				DomainModelObjectMapperFactoryFactory.logger.info("Building DomainModelObjectMapper...");
				// Document accessors
				Documents.registerAccessor(Project.class, new ProjectDocumentAccessor());
				Documents.registerAccessor(DomainModel.class, new DomainModelDocumentAccessor());
				Documents.registerAccessor(BindingGraph.class, new BindingGraphDocumentAccessor());
				Documents.registerAccessor(VersionedSetWrapper.class, new VersionedSetWrapperDocumentAccessor());
				
				this.domainModelObjectMapperFactory = new DomainModelObjectMapperFactory();
				
				DomainModelObjectMapper.getInstance().setObjectMapper(this.domainModelObjectMapperFactory.createObjectMapper());
				// If I remove this statement the org.ektorp.DbAccessException:
				// com.fasterxml.jackson.databind.JsonMappingException: Unexpected token
				// (END_OBJECT), expected FIELD_NAME: missing property '@class' that is
				// to contain type id (for class
				// de.bitub.proitbau.common.versioning.model.Versionable)
				DomainModelObjectMapper
					.getInstance()
					.getObjectMapper()
					.enableDefaultTypingAsProperty(DefaultTyping.JAVA_LANG_OBJECT, JsonTypeInfo.Id.CLASS.getDefaultPropertyName());
				
				// @formatter:off
				DomainModelObjectMapper.getInstance().addMixInAnnotation(iModelObject.class, iModelObjectMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(iTemplateInstance.class, iTemplateInstanceMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(iNamedModelObject.class, iNamedModelObjectMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(iProjectCalendar.class, iProjectCalendarMixIn.class);
				
				DomainModelObjectMapper.getInstance().addMixInAnnotation(aObservable.class, aObservableMixIn.class);
				
				DomainModelObjectMapper.getInstance().addMixInAnnotation(iStructuringSet.class,	iStructuringSetMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(StructuringSet.class,	StructuringSetMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(iStructuringTree.class, iStructuringTreeMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(StructuringTree.class, StructuringTreeMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(iStructuringNode.class, iStructuringNodeMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(StructuringNode.class, StructuringNodeMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(iStructuringObject.class, iStructuringObjectMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(StructuringObject.class, StructuringObjectMixIn.class); 
				
				DomainModelObjectMapper.getInstance().addMixInAnnotation(iPersistable.class, iPersistableMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(Versionable.class, VersionableMixIn.class);
				
				DomainModelObjectMapper.getInstance().addMixInAnnotation(aChecklistedModelObject.class, aChecklistedModelObjectMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(aModelObject.class, aModelObjectMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(aNamedModelObject.class, aNamedModelObjectMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(aTemplateInstance.class, aTemplateInstanceMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(DomainModel.class, DomainModelMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(iDomainModel.class, iDomainModelMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(iUpdateInformation.class, iUpdateInformationMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(iUpdate.class, iUpdateMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(UpdateInformation.class, UpdateInformationMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(Update.class, UpdateMixIn.class);
				
				DomainModelObjectMapper.getInstance().addMixInAnnotation(aActivity.class, aActivityMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(aActivityState.class, aActivityStateMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(aState.class, aStateMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(PDActivity.class, PDActivityMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(PIActivity.class, PIActivityMixIn.class);
				
				DomainModelObjectMapper.getInstance().addMixInAnnotation(aProcessComponent.class, aProcessComponentMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(aProcessComposite.class, aProcessCompositeMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(aProcess.class, aProcessMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(PIProcess.class, PIProcessMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(PDProcess.class, PDProcessMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(aSubprocess.class, aSubprocessMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(PDSubprocess.class, PDSubprocessMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(PISubprocess.class, PISubprocessMixIn.class);
				
				DomainModelObjectMapper.getInstance().addMixInAnnotation(SemanticEdge.class, SemanticEdgeMixIn.class);
				
				DomainModelObjectMapper.getInstance().addMixInAnnotation(aProcessNode.class, aProcessNodeMixIn.class);
				
				DomainModelObjectMapper.getInstance().addMixInAnnotation(aTripleProcessNode.class, aTripleProcessNodeMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(PDTripleProcessNode.class, PDTripleProcessNodeMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(PITripleProcessNode.class, PITripleProcessNodeMixIn.class);
				
				DomainModelObjectMapper.getInstance().addMixInAnnotation(StructuringSetPersistable.class,	StructuringSetPersistableMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(StructuringNodePersistable.class, StructuringNodePersistableMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(StructuringObjectPersistable.class, StructuringObjectPersistableMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(StructuringTreePersistable.class, StructuringTreePersistableMixIn.class);
				
				DomainModelObjectMapper.getInstance().addMixInAnnotation(aSubject.class, aSubjectMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(PDSubject.class, PDSubjectMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(PISubject.class, PISubjectMixIn.class);
				
				DomainModelObjectMapper.getInstance().addMixInAnnotation(ProjectCalendar.class, ProjectCalendarMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(WeeklyShift.class, WeeklyShiftMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(DailyShifts.class, DailyShiftsMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(Shift.class, ShiftMixIn.class);
				
				DomainModelObjectMapper.getInstance().addMixInAnnotation(Project.class, ProjectMixIn.class);

				// Versioning classes
				DomainModelObjectMapper.getInstance().addMixInAnnotation(BindingGraph.class, BindingGraphMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(iBindingGraph.class, iBindingGraphMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(VersionedSetRepresentation.class, VersionedSetRepresentationMixIn.class);
				DomainModelObjectMapper.getInstance().addMixInAnnotation(VersionedSetWrapper.class, VersionedSetWrapperMixIn.class);
				
				DomainModelObjectMapper.getInstance().addMixInAnnotation(VersionedSet.class, VersionedSetMixIn.class);
				// @formatter:on
			}
			DomainModelObjectMapperFactoryFactory.logger.info("Building DomainModelObjectMapper finished...");
			return this.domainModelObjectMapperFactory;
		}
		return null;
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] {
			iDomainModelObjectMapperFactory.class
		};
	}
}
