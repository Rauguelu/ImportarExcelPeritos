package com.justicia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class AsignaEspecialidades {
	
	public static void main(String[] args) {
		
		// Conexion a la base de datos MySQL
		String url = "jdbc:mysql://:3306/Peritos2013";
		String user = "root";
		String password = "gtjclm";
		List<String> listadoTerminos  = new ArrayList<String>();
		
	
		try {
			
			listadoTerminos = getListadoTerminos();
			
			
			// Leemos los peritos uno a uno y vamos encontrando
			String sql = "SELECT IdPerito,COALESCE(NULLIF(especialidades, ''), nombre_agrupacion_profesional) AS especialidades  FROM Peritos";
			System.out.println("Cadena SQL: " + sql);
			
			// Conectar a la base de datos
			Connection con = DriverManager.getConnection(url, user, password);
			
			PreparedStatement preparedStmt = con.prepareStatement(sql);
			ResultSet result = preparedStmt.executeQuery(sql);
			
			
			while(result.next()) {
				
				ArrayList < String > arrayEspecialidades = new ArrayList <String> ();
				String especialidad = result.getString("especialidades");
				String idPerito = result.getString("IdPerito");
				
				for(int i=0; i<listadoTerminos.size(); i++) {
				String[] termino = listadoTerminos.get(i).split("¬");
					
					if(termino != null 
								&& termino.length > 1 
									&& contiene(especialidad.toUpperCase(), termino[1].toUpperCase())) {
						arrayEspecialidades.add(termino[0]);
					}
				}
				
				// quita repetidos
				arrayEspecialidades = eliminarRepetidos(arrayEspecialidades);
				System.out.println("Intentamos categorizar a idPerito: " + idPerito);
				
				for (int j = 0; j < arrayEspecialidades.size(); j++) {

					// Preparar la consulta SQL para insertar los datos en la tabla "EspecialidadesxPeritos"
					String query = "INSERT INTO EspecialidadesxPeritos (IdPerito,IdEspecialidad) VALUES ('"
							+ idPerito + "','" + arrayEspecialidades.get(j) + "')";

					System.out.println("Cadena query: " + query);
					PreparedStatement pstmt = con.prepareStatement(query);
					// Ejecutar la consulta SQL
					pstmt.executeUpdate();

				}
				
			}
			

			String query = "select idPerito from Peritos where idPerito not in (select idPerito from EspecialidadesxPeritos) order by idPerito asc";
			System.out.println("Cadena SQL: " + query);
			
			preparedStmt = con.prepareStatement(query);
			result = preparedStmt.executeQuery(query);
			System.out.println("Perito sin categorizar: " + result.getString("IdPerito"));
			
			
			while(result.next()) {
				
			}
			
			// Cerrar la conexi�n a la base de datos
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	


	public static boolean contiene(String cadena, String subcadena) {
		String cadenaNormalizada = Normalizer.normalize(cadena, Normalizer.Form.NFD)
				.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase();
		String subcadenaNormalizada = Normalizer.normalize(subcadena, Normalizer.Form.NFD)
				.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase();
		return cadenaNormalizada.contains(subcadenaNormalizada);
	}

	
	 public static ArrayList<String> eliminarRepetidos(ArrayList<String> lista) {
	        // Crear un HashSet temporal para almacenar elementos �nicos
	        HashSet<String> conjunto = new HashSet<String>(lista);

	        // Limpiar el ArrayList original
	        lista.clear();

	        // Agregar elementos �nicos de nuevo al ArrayList
	        lista.addAll(conjunto);
	        return lista;
	    }

	
	// Obtiene la lista de cursos solicitados por cada solicitante
	public static List<String> getListadoTerminos() throws SQLException {

		// Conexi�n a la base de datos MySQL
		String url = "jdbc:mysql://10.44.130.21:3306/Peritos2013";
		String user = "root";
		String password = "gtjclm";
		List<String> listadoTerminos = new ArrayList<String>();

		// Conectar a la base de datos
		Connection connection = DriverManager.getConnection(url, user, password);

		try {

			if (connection != null) {

				// Consulta la lista de cursos solicitados por cada solicitante
				String query = "select distinct concat(idEspecialidad,'¬',Termino) as cadena from Especialidades_x_terminos";

				PreparedStatement preparedStmt = connection.prepareStatement(query);
				ResultSet result = preparedStmt.executeQuery(query);

				while (result.next()) {

					listadoTerminos.add(result.getString("cadena"));
				}

			} else { // Sino informamos que no nos podemos conectar.
				System.out.println("No has podido conectarte - getListadoCursosSolicitadosPorSolicitante()");
			}

		} catch (SQLException ex) {
			ex.printStackTrace();
		} finally {
			connection.close();
		}

		return listadoTerminos;
	}
}
