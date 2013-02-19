package wp.gmt;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import org.bridgedb.BridgeDb;
import org.bridgedb.DataSource;
import org.bridgedb.IDMapper;
import org.bridgedb.IDMapperException;
import org.bridgedb.Xref;
import org.bridgedb.bio.BioDataSource;
import org.pathvisio.core.model.ConverterException;
import org.pathvisio.core.model.Pathway;


public class GmtFileCreator {

	/**
	 * needs for parameters:
	 * pathwayDir = directory
	 * outputFile = file
	 * bridgeDbMapper = file
	 * sysCode
	 */
	public static void main (String [] args) {
		try{
			if(args.length == 4) {
				File pathwayDir = new File(args[0]);
				File outputFile = new File(args[1]);
				outputFile.createNewFile();
				File bridgeDbMapper = new File(args[2]);
				String sysCode = args[3];
				System.out.println(pathwayDir.getAbsolutePath());
				System.out.println(outputFile.getAbsolutePath());
				System.out.println(bridgeDbMapper);
				System.out.println(sysCode);
				
				File logFile = new File(outputFile.getParentFile(), "gmt-log.txt");
				
				BufferedWriter outWriter = new BufferedWriter(new FileWriter(outputFile));
				BufferedWriter logWriter = new BufferedWriter(new FileWriter(logFile));		
				
				try {
					if(bridgeDbMapper.exists()) {
						if(pathwayDir.exists() && pathwayDir.isDirectory()) {
							// initialize bridgedb
							System.out.println("init bridgedb");
							BioDataSource.init();
							System.out.println("init bridgedb");
							Class.forName("org.bridgedb.rdb.IDMapperRdb");
							IDMapper mapper = BridgeDb.connect("idmapper-pgdb:" + bridgeDbMapper.getAbsolutePath());
							for (File file : pathwayDir.listFiles()) {
								System.out.println(file);
								if(file.getName().endsWith("gpml")) {
									Pathway pathway = new Pathway();
									pathway.setSourceFile(file);
									pathway.readFromXml(file, false);
									logWriter.write(pathway.getSourceFile().getName() + "\n---------------------------------\n");
									String genes = "";
									for(Xref xref : pathway.getDataNodeXrefs()) {
										if(xref != null && xref.getId() != null & !xref.getId().equals("") && xref.getDataSource() != null) {
											Set<Xref> result = mapper.mapID(xref, DataSource.getBySystemCode(sysCode));
											if(result.size() > 0) {
												for(Xref x : result) {
													genes = genes + x.getId() + "\t";
												}
											} else {
												logWriter.write("Couldn't map " + xref + "\n");
											}
										}
									}
									logWriter.write("-----------------------------------\n\n");
									if(!genes.equals("")) {
										outWriter.write(pathway.getSourceFile().getName().replace(",", "") + "\t" + "NA" + "\t" + genes + "\n");
									}
								}
							} 
						} else {
							System.out.println("\n\nERROR: Pathway directory does not exist.\t" + pathwayDir.getAbsolutePath());
							logWriter.write("\n\nERROR: Pathway directory does not exist.\t" + pathwayDir.getAbsolutePath()+"\n");
						}
					} else {
						System.out.println("\n\nERROR: BridgeDb file does not exist.\t" + bridgeDbMapper.getAbsolutePath());
						logWriter.write("\n\nERROR: BridgeDb file does not exist.\t" + bridgeDbMapper.getAbsolutePath()+"\n");
					}
				} catch (ClassNotFoundException e) {
					System.out.println("\n\nERROR: Could not initialize BridgeDb.");
					logWriter.write("\n\nERROR: Could not initialize BridgeDb.\n");
				} catch (ConverterException e) {
					
				} catch (IDMapperException e) {
					System.out.println("\n\nERROR: Could not connect to BridgeDb database.");
					logWriter.write("\n\nERROR: Could not connect to BridgeDb database.\n");
				} finally {
					logWriter.close();
					outWriter.close();
				}
			} else {
				System.out.println("Invalid input: pathwayDir, outputFile, bridgeDbFile, systemcode");
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
