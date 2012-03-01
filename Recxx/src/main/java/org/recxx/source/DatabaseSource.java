package org.recxx.source;

import java.util.List;
import java.util.Set;

import org.recxx.domain.Key;
import org.recxx.domain.Column;

public class DatabaseSource implements Source<Key> {

	// TODO Implement

	public Source<Key> call() throws Exception {
		return null;
	}

	public Set<Key> getKeySet() {
		return null;
	}

	public List<?> getRow(Key key) {
		return null;
	}

	public List<Column<String, Class<?>>> getColumns() {
		return null;
	}

	public String getAlias() {
		return null;
	}

	public List<String> getKeyColumns() {
		return null;
	}

	public List<String> getCompareColumns() {
		return null;
	}

}
