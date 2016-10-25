package aima.gui.swing.demo.agent.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.filechooser.FileNameExtensionFilter;

import aima.core.environment.map.AdaptableHeuristicFunction;
import aima.core.environment.map.ExtendableMap;
import aima.core.environment.map.MapAgent;
import aima.core.environment.map.MapEnvironment;
import aima.core.environment.map.Scenario;
import aima.core.environment.map.SimplifiedRoadMapOfAustralia;
import aima.core.environment.map.SimplifiedRoadMapOfPartOfRomania;
import aima.core.util.math.geom.shapes.Point2D;
import aima.gui.swing.framework.AgentAppController;
import aima.gui.swing.framework.AgentAppEnvironmentView;
import aima.gui.swing.framework.AgentAppFrame;
import aima.gui.swing.framework.MessageLogger;
import aima.gui.swing.framework.SimpleAgentApp;
import javafx.stage.FileChooser;

/**
 * Demo example of a route finding agent application with GUI. The main method
 * starts a map agent frame and supports runtime experiments. This
 * implementation is based on the {@link aima.core.environment.map.MapAgent} and
 * the {@link aima.core.environment.map.MapEnvironment}. It can be used as a
 * code template for creating new applications with different specialised kinds
 * of agents and environments.
 * 
 * @author Ruediger Lunde
 */
public class RouteFindingAgentApp extends SimpleAgentApp {

	/** Creates a <code>MapAgentView</code>. */
	public AgentAppEnvironmentView createEnvironmentView() {
		return new ExtendedMapAgentView();
	}
	
	/** Creates and configures a <code>RouteFindingAgentFrame</code>. */
	@Override
	public AgentAppFrame createFrame() {
		return new RouteFindingAgentFrame();
	}

	/** Creates a <code>RouteFindingAgentController</code>. */
	@Override
	public AgentAppController createController() {
		return new RouteFindingAgentController();
	}
	
	public RouteFindingAgentApp() {
		//Load australia and romania maps
		loadedMaps = new ArrayList<>(Arrays.asList(
				new LoadedMap(new SimplifiedRoadMapOfAustralia(), "Australia"),
				new LoadedMap(new SimplifiedRoadMapOfPartOfRomania(), "Romania")
				));
	}
	
	/**
	 * List of all loaded maps
	 */
	private List<LoadedMap> loadedMaps; 
	
	/**
	 * Class to hold a map info
	 *
	 */
	private static class LoadedMap {
		public final ExtendableMap MAP;
		public final String TITLE;
		public LoadedMap(ExtendableMap map, String title) {
			TITLE = title;
			MAP = map;
		}
 	}
	
	//ExtendableMap
	// //////////////////////////////////////////////////////////
	// local classes

	/** Frame for a graphical route finding agent application. */
	protected class RouteFindingAgentFrame extends MapAgentFrame {
		private static final long serialVersionUID = 1L;
		/** Creates a new frame. */
		public RouteFindingAgentFrame() {
			setTitle("RFA - the Route Finding Agent");
			updateScenarioSelector();
			setSelectorItems(Q_SEARCH_IMPL_SEL, SearchFactory.getInstance()
					.getQSearchImplNames(), 1); // change the default!
			setSelectorItems(HEURISTIC_SEL, new String[] { "=0", "SLD" }, 1);
		}
		
		protected void updateScenarioSelector() {
			String[] scenarios = new String[loadedMaps.size() + 1];
			int i = 0;
			for (LoadedMap map : loadedMaps)
				scenarios[i++] = map.TITLE;
			scenarios[i] = "LOAD FROM FILE";
			setSelectorItems(SCENARIO_SEL, scenarios, 0);
		}

		/**
		 * Changes the destination selector items depending on the scenario
		 * selection if necessary, and calls the super class implementation
		 * afterwards.
		 */
		@Override
		protected void selectionChanged(String changedSelector) {
			System.out.println("Changed: " + changedSelector);
			if (changedSelector == null || changedSelector.equals(SCENARIO_SEL)) {
				SelectionState state = getSelection();
				int scenarioIdx = state.getIndex(MapAgentFrame.SCENARIO_SEL);
				if (scenarioIdx == loadedMaps.size()) {
					//Ask for file
					String file = getFile(new FileNameExtensionFilter("MAP txt files", "txt"));
					System.out.println(file);
					updateScenarioSelector();
				}
				else {
					setSelectorItems(ORIGIN_SEL, loadedMaps.get(scenarioIdx).MAP.getLocations().toArray(), 0);
					setSelectorItems(DESTINATION_SEL, loadedMaps.get(scenarioIdx).MAP.getLocations().toArray(), 0);
				}
				
			}
			else if (changedSelector.equals(ORIGIN_SEL)) {
				SelectionState state = getSelection();
				int scenarioIdx = state.getIndex(MapAgentFrame.ORIGIN_SEL);
			}
			else if (changedSelector.equals(DESTINATION_SEL)) {
				SelectionState state = getSelection();
				int scenarioIdx = state.getIndex(MapAgentFrame.ORIGIN_SEL);
			}
			
			super.selectionChanged(changedSelector);
		}
	}

	/** Controller for a graphical route finding agent application. */
	protected class RouteFindingAgentController extends
			AbstractMapAgentController {
		/**
		 * Configures a scenario and a list of destinations. Note that for route
		 * finding problems, the size of the list needs to be 1.
		 */
		@Override
		protected void selectScenarioAndDest(int scenarioIdx, int originIdx, int destIdx) {
			ExtendableMap map = loadedMaps.get(scenarioIdx).MAP;
			MapEnvironment env = new MapEnvironment(map);
			String agentLoc = null;
			
			scenario = new Scenario(env, map, map.getLocations().get(originIdx));

			destinations = new ArrayList<String>();
			destinations.add(map.getLocations().get(destIdx));
			System.out.println("Ok..");
		}

		/**
		 * Prepares the view for the previously specified scenario and
		 * destinations.
		 */
		@Override
		protected void prepareView() {
			ExtendedMapAgentView mEnv = (ExtendedMapAgentView) frame.getEnvView();
			mEnv.setData(scenario, destinations, null);
			mEnv.setEnvironment(scenario.getEnv());
		}

		/**
		 * Returns the trivial zero function or a simple heuristic which is
		 * based on straight-line distance computation.
		 */
		@Override
		protected AdaptableHeuristicFunction createHeuristic(int heuIdx) {
			AdaptableHeuristicFunction ahf = null;
			switch (heuIdx) {
			case 0:
				ahf = new H1();
				break;
			default:
				ahf = new H2();
			}
			return ahf.adaptToGoal(destinations.get(0), scenario
					.getAgentMap());
		}

		/**
		 * Creates a new agent and adds it to the scenario's environment.
		 */
		@Override
		public void initAgents(MessageLogger logger) {
			if (destinations.size() != 1) {
				logger.log("Error: This agent requires exact one destination.");
				return;
			}
			MapEnvironment env = scenario.getEnv();
			String goal = destinations.get(0);
			MapAgent agent = new MapAgent(env.getMap(), env, search, new String[] { goal });
			env.addAgent(agent, scenario.getInitAgentLocation());
		}
	}

	/**
	 * Returns always the heuristic value 0.
	 */
	static class H1 extends AdaptableHeuristicFunction {

		public double h(Object state) {
			return 0.0;
		}
	}

	/**
	 * A simple heuristic which interprets <code>state</code> and {@link #goal}
	 * as location names and uses the straight-line distance between them as
	 * heuristic value.
	 */
	static class H2 extends AdaptableHeuristicFunction {

		public double h(Object state) {
			double result = 0.0;
			Point2D pt1 = map.getPosition((String) state);
			Point2D pt2 = map.getPosition((String) goal);
			if (pt1 != null && pt2 != null)
				result = pt1.distance(pt2);
			return result;
		}
	}

	// //////////////////////////////////////////////////////////
	// starter method

	/** Application starter. */
	public static void main(String args[]) {
		new RouteFindingAgentApp().startApplication();
	}
}
